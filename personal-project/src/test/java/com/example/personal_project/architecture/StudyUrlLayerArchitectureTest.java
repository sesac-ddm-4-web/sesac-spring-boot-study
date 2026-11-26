package com.example.personal_project.architecture;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class StudyUrlLayerArchitectureTest {
    private static final Path SOURCE = Path.of("src/main/java/com/example/personal_project");

    @Test
    void domainHasNoFrameworkOrOuterLayerImports() throws Exception {
        check(List.of("domain/study", "domain/url"), List.of(
                "org.springframework", "jakarta.", "java.sql.", ".presentation.", ".application.", ".infrastructure."));
    }

    @Test
    void applicationDoesNotImportHttpOrPersistenceImplementations() throws Exception {
        check(List.of("application/study", "application/url"), List.of(
                ".presentation.", ".infrastructure.", "org.springframework.jdbc", "org.springframework.security",
                "org.springframework.web", "org.springframework.http"));
    }

    @Test
    void controllersDoNotImportRepositoriesOrTransactions() throws Exception {
        check(List.of("presentation/controller/study", "presentation/controller/url"), List.of(
                ".repository.", "org.springframework.jdbc", "org.springframework.transaction"));
    }

    private void check(List<String> directories, List<String> forbidden) throws Exception {
        for (String directory : directories) {
            try (var paths = Files.walk(SOURCE.resolve(directory))) {
                for (Path file : paths.filter(p -> p.toString().endsWith(".java")).toList()) {
                    for (String line : Files.readAllLines(file)) {
                        if (line.startsWith("import ")) {
                            for (String dependency : forbidden)
                                assertFalse(line.contains(dependency), file + " has forbidden dependency: " + line);
                        }
                    }
                }
            }
        }
    }
}
