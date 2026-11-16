package com.example.personal_project.study;

import com.example.personal_project.domain.*;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import java.util.Map;
import java.time.LocalDateTime;

@Configuration
@ConditionalOnProperty(name="study.demo.enabled",havingValue="true")
public class StudyDemoInitializer {
    @Bean ApplicationRunner studyDemo(StudyStore store,NamedParameterJdbcTemplate jdbc,PasswordEncoder encoder,
            StudySessionRepository sessions,ShortLinkRepository links,PlatformTransactionManager manager,
            @Value("${STUDY_DEMO_PASSWORD:}") String password) {
        return args -> {
            if(password.length()<10) throw new IllegalArgumentException("STUDY_DEMO_PASSWORD를 10자 이상으로 지정해주세요.");
            new TransactionTemplate(manager).executeWithoutResult(status -> {
                String[] logins={"admin01","presenter01","presenter02"};
                for(int i=0;i<logins.length;i++) {
                    if(store.memberByLogin(logins[i])==null) jdbc.update("""
                            INSERT INTO members(login_id,password_hash,display_name,role,created_at)
                            VALUES(:login,:hash,:name,:role,UTC_TIMESTAMP(3))
                            """,Map.of("login",logins[i],"hash",encoder.encode(password),
                            "name",i==0?"스터디 운영자":"발표자 "+i,"role",i==0?"ADMIN":"PRESENTER"));
                }
                if(links.findByCode("welcome-study")==null) {
                    int no=jdbc.queryForObject("SELECT COALESCE(MAX(session_no),0)+1 FROM study_sessions",Map.of(),Integer.class);
                    long admin=store.memberByLogin("admin01").id();
                    long presenter=store.memberByLogin("presenter01").id();
                    long sid=sessions.insert(new StudySession(null,no,"함께 시작하는 Spring 스터디",
                            LocalDateTime.now(java.time.ZoneOffset.UTC).plusDays(1).withNano(0),
                            "회차 안내와 발표자료를 한곳에서 확인하세요. 발표자는 배정된 회차에 자료를 등록할 수 있습니다.",
                            "스터디룸 A",null,"노트북과 실습 환경을 준비해주세요.",admin,null,null));
                    links.insert(new ShortLink(null,"welcome-study",sid,null,0));
                    store.assign(sid,presenter);
                    long mid=store.insertMaterial(sid,presenter,"Spring 공식 학습 가이드","https://spring.io/guides");
                    links.insert(new ShortLink(null,"welcome-spring",null,mid,0));
                }
            });
        };
    }
}
