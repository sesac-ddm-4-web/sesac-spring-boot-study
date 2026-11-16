package com.example.personal_project.application;

import com.example.personal_project.domain.StudySession;
import com.example.personal_project.global.exception.DuplicateShortenUrlCodeException;
import com.example.personal_project.global.exception.LackOfShortenUrlKeyException;
import com.example.personal_project.global.exception.ShortLinkCodeConflictException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.regex.Pattern;

@Service
public class StudySessionRegistrationService {
    private static final int MAX_ATTEMPTS = 5;
    private static final Pattern CUSTOM_CODE = Pattern.compile("^[a-z0-9]+(?:-[a-z0-9]+)*$");

    private final StudySessionCreationService creation;
    private final ShortCodeGenerator generator;

    public StudySessionRegistrationService(StudySessionCreationService creation, ShortCodeGenerator generator) {
        this.creation = creation;
        this.generator = generator;
    }

    /** 각 create 호출이 별도의 트랜잭션을 시작하도록 호출자의 트랜잭션을 잠시 중단한다. */
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public Result register(StudySession session, String requestedCode) {
        if (session == null) {
            throw new IllegalArgumentException("회차 정보가 필요합니다.");
        }
        if (requestedCode != null) {
            String code = requestedCode.trim();
            if (code.length() < 3 || code.length() > 40 || !CUSTOM_CODE.matcher(code).matches()) {
                throw new IllegalArgumentException("단축 코드는 3~40자의 소문자·숫자·하이픈 형식이어야 합니다.");
            }
            try {
                return new Result(creation.create(session, code), code);
            } catch (ShortLinkCodeConflictException exception) {
                throw new DuplicateShortenUrlCodeException("이미 사용 중인 단축 주소입니다.");
            }
        }

        for (int attempt = 0; attempt < MAX_ATTEMPTS; attempt++) {
            String code = generator.generate();
            try {
                return new Result(creation.create(session, code), code);
            } catch (ShortLinkCodeConflictException exception) {
                // create 트랜잭션의 롤백이 끝난 뒤 다음 코드를 시도한다.
            }
        }
        throw new LackOfShortenUrlKeyException("URL 생성에 실패했습니다.");
    }

    public record Result(Long sessionId, String code) {
    }
}
