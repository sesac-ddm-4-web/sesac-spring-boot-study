package com.example.personal_project.application.study;

import com.example.personal_project.application.url.port.ShortCodeGenerator;
import com.example.personal_project.domain.study.model.StudySession;
import com.example.personal_project.domain.url.exception.DuplicateShortenUrlCodeException;
import com.example.personal_project.domain.url.exception.LackOfShortenUrlKeyException;
import com.example.personal_project.domain.url.exception.ShortLinkCodeConflictException;
import com.example.personal_project.domain.url.model.ShortCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StudySessionRegistrationService {
    private static final int MAX_ATTEMPTS = 5;

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
            String code = new ShortCode(requestedCode).value();
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
