package com.example.shorten_url_service.infrastructure;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

@Component
public class PdfFileStorage {
    private static final Duration PDF_VALIDITY = Duration.ofMinutes(10);
    private static final long MAX_FILE_SIZE = 10L * 1024 * 1024;
    private static final Logger log = LoggerFactory.getLogger(PdfFileStorage.class);

    private final Path directory;

    public PdfFileStorage() {
        this(Path.of(System.getProperty("java.io.tmpdir"), "print-link"));
    }

    PdfFileStorage(Path directory) {
        this.directory = directory;
    }

    public String save(MultipartFile file) throws IOException {
        validatePdf(file);
        Files.createDirectories(directory);

        Path temporaryFile = Files.createTempFile(directory, "pdf-", ".part");
        String fileName = temporaryFile.getFileName().toString().replace(".part", ".pdf");
        Path filePath = directory.resolve(fileName);

        try {
            file.transferTo(temporaryFile);
            Files.setLastModifiedTime(temporaryFile, FileTime.from(Instant.now()));
            Files.move(temporaryFile, filePath);
            return fileName;
        } catch (IOException | RuntimeException e) {
            try {
                Files.deleteIfExists(temporaryFile);
            } catch (IOException deleteException) {
                e.addSuppressed(deleteException);
            }
            throw e;
        }
    }

    private void validatePdf(MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("빈 파일입니다.");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("PDF는 최대 10MB까지 업로드할 수 있습니다.");
        }

        try (InputStream inputStream = file.getInputStream()) {
            String header = new String(inputStream.readNBytes(5), StandardCharsets.US_ASCII);

            if (!header.equals("%PDF-")) {
                throw new IllegalArgumentException("PDF 파일만 업로드할 수 있습니다.");
            }
        }
    }

    public String createCancelToken(String fileName) throws IOException {
        if (!isPdfFileName(fileName)) {
            throw new IllegalArgumentException("잘못된 PDF 파일명입니다.");
        }

        String token = UUID.randomUUID().toString();
        Files.write(directory.resolve(fileName + ".cancel"), hashToken(token),
                java.nio.file.StandardOpenOption.CREATE_NEW);
        return token;
    }

    public void protect(String fileName, String pin) throws IOException {
        if (pin == null || !pin.matches("[0-9]{4}")) {
            throw new IllegalArgumentException("PIN은 숫자 4자리로 입력하세요.");
        }
        byte[] salt = new byte[16];
        new java.security.SecureRandom().nextBytes(salt);
        byte[] record = java.nio.ByteBuffer.allocate(48).put(salt).put(pinHash(pin, salt)).array();
        Files.write(directory.resolve(fileName + ".pin"), record, java.nio.file.StandardOpenOption.CREATE_NEW);
    }

    private byte[] pinHash(String pin, byte[] salt) {
        var spec = new javax.crypto.spec.PBEKeySpec(pin.toCharArray(), salt, 210_000, 256);
        try {
            return javax.crypto.SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).getEncoded();
        } catch (java.security.GeneralSecurityException e) {
            throw new IllegalStateException(e);
        } finally {
            spec.clearPassword();
        }
    }

    public boolean requiresPin(String name) {
        return isPdfFileName(name) && !Files.notExists(directory.resolve(name + ".pin"));
    }

    private final java.util.concurrent.ConcurrentMap<String, Integer> failures =
            new java.util.concurrent.ConcurrentHashMap<>();

    public boolean verifyPin(String name, String pin) throws IOException {
        if (load(name) == null || pin == null || !pin.matches("[0-9]{4}")) return false;
        byte[] record = Files.readAllBytes(directory.resolve(name + ".pin"));
        // Reserve an attempt before hashing so simultaneous requests share the limit.
        if (failures.merge(name, 1, (count, one) -> Math.min(count + 1, 6)) > 5) return false;
        boolean valid = record.length == 48 && MessageDigest.isEqual(
                java.util.Arrays.copyOfRange(record, 16, 48),
                pinHash(pin, java.util.Arrays.copyOf(record, 16)));
        if (valid) failures.remove(name);
        return valid;
    }

    public boolean cancel(String fileName, String token) throws IOException {
        if (!isPdfFileName(fileName) || token == null || token.length() != 36) {
            return false;
        }

        byte[] savedHash;
        try {
            savedHash = Files.readAllBytes(directory.resolve(fileName + ".cancel"));
        } catch (NoSuchFileException e) {
            return false;
        }

        if (!MessageDigest.isEqual(savedHash, hashToken(token))) {
            return false;
        }

        delete(fileName);
        return true;
    }

    public void delete(String fileName) throws IOException {
        if (!isPdfFileName(fileName)) {
            throw new IllegalArgumentException("잘못된 PDF 파일명입니다.");
        }
        Files.deleteIfExists(directory.resolve(fileName));
        Files.deleteIfExists(directory.resolve(fileName + ".cancel"));
        Files.deleteIfExists(directory.resolve(fileName + ".pin"));
        failures.remove(fileName);
    }

    private byte[] hashToken(String token) {
        try {
            return MessageDigest.getInstance("SHA-256")
                    .digest(token.getBytes(StandardCharsets.US_ASCII));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256을 사용할 수 없습니다.", e);
        }
    }

    public Resource load(String fileName) {
        if (!isPdfFileName(fileName)) {
            return null;
        }

        Path filePath = directory.resolve(fileName);

        try {
            if (!Files.isRegularFile(filePath, LinkOption.NOFOLLOW_LINKS) || isExpired(filePath)) {
                return null;
            }

            return new FileSystemResource(filePath);
        } catch (NoSuchFileException e) {
            return null;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Scheduled(fixedDelay = 60_000)
    public void deleteExpiredFiles() {
        if (!Files.isDirectory(directory, LinkOption.NOFOLLOW_LINKS)) {
            return;
        }

        try (DirectoryStream<Path> files = Files.newDirectoryStream(directory, "pdf-*")) {
            for (Path filePath : files) {
                String fileName = filePath.getFileName().toString();

                if (!fileName.matches("pdf-[0-9]+\\.(pdf|part|pdf\\.cancel)")
                        || !Files.isRegularFile(filePath, LinkOption.NOFOLLOW_LINKS)) {
                    continue;
                }

                try {
                    if (isExpired(filePath)) {
                        if (isPdfFileName(fileName)) {
                            delete(fileName);
                        } else {
                            Files.deleteIfExists(filePath);
                        }
                    }
                } catch (NoSuchFileException e) {
                    // 이미 삭제된 파일은 다음 파일로 넘어간다.
                } catch (IOException e) {
                    log.warn("만료 파일 삭제 실패: {}", fileName, e);
                }
            }
        } catch (IOException e) {
            log.warn("임시 PDF 저장소 조회 실패", e);
        }
    }

    private boolean isPdfFileName(String fileName) {
        return fileName != null && fileName.matches("pdf-[0-9]+\\.pdf");
    }

    private boolean isExpired(Path filePath) throws IOException {
        Instant savedAt = Files.getLastModifiedTime(filePath).toInstant();
        Instant expiresAt = savedAt.plus(PDF_VALIDITY);
        return !Instant.now().isBefore(expiresAt);
    }
}
