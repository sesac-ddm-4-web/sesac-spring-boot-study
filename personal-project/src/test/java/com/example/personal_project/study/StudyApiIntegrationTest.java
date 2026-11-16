package com.example.personal_project.study;

import com.example.personal_project.application.ShortCodeGenerator;
import com.example.personal_project.global.exception.LackOfShortenUrlKeyException;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.Configuration;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.jdbc.core.namedparam.*;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.*;
import org.springframework.mock.web.MockHttpSession;
import java.util.*;
import java.util.concurrent.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class StudyApiIntegrationTest {
    @Autowired MockMvc mvc;
    @Autowired NamedParameterJdbcTemplate jdbc;
    @Autowired PasswordEncoder passwords;
    @Autowired MaterialCreationService creation;
    @Autowired StudyRedirectController.Resolver resolver;
    @Autowired com.example.personal_project.application.StudySessionRegistrationService registration;
    StudyPrincipal admin,a,b;
    int number;
    final String password="test-password-2026";

    @BeforeEach void fixtures() {
        admin=member("ADMIN"); a=member("PRESENTER"); b=member("PRESENTER");
        number=jdbc.queryForObject("SELECT COALESCE(MAX(session_no),0)+1 FROM study_sessions",Map.of(),Integer.class);
    }
    StudyPrincipal member(String role) {
        String login="qa-"+UUID.randomUUID(); String hash=passwords.encode(password);
        var keys=new GeneratedKeyHolder();
        jdbc.update("""
                INSERT INTO members(login_id,password_hash,display_name,role,created_at)
                VALUES(:login,:hash,:name,:role,UTC_TIMESTAMP(3))
                """,new MapSqlParameterSource().addValue("login",login).addValue("hash",hash)
                .addValue("name","테스트 "+role).addValue("role",role),keys,new String[]{"id"});
        return new StudyPrincipal(keys.getKey().longValue(),login,hash,"테스트 "+role,role);
    }
    @AfterEach void cleanup() {
        if(admin==null) return;
        var args=Map.of("uid",admin.id());
        jdbc.update("DELETE FROM short_links WHERE material_id IN (SELECT m.id FROM materials m JOIN study_sessions s ON m.session_id=s.id WHERE s.created_by=:uid)",args);
        jdbc.update("DELETE FROM short_links WHERE session_id IN (SELECT id FROM study_sessions WHERE created_by=:uid)",args);
        jdbc.update("DELETE FROM materials WHERE session_id IN (SELECT id FROM study_sessions WHERE created_by=:uid)",args);
        jdbc.update("DELETE FROM session_presenters WHERE session_id IN (SELECT id FROM study_sessions WHERE created_by=:uid)",args);
        jdbc.update("DELETE FROM study_sessions WHERE created_by=:uid",args);
        for(var principal:Arrays.asList(a,b,admin)) if(principal!=null) jdbc.update("DELETE FROM members WHERE id=:id",Map.of("id",principal.id()));
    }
    String code() { return "qa-"+UUID.randomUUID().toString().replace("-",""); }
    String json(Map<String,?> data) { return Configuration.defaultConfiguration().jsonProvider().toJson(data); }
    Map<String,Object> sessionBody(String code) {
        var map=new LinkedHashMap<String,Object>(); map.put("sessionNo",number);map.put("title","JPA 100% 스터디");
        map.put("startsAt","2026-11-20T14:00:00+09:00");map.put("notice","발표 준비 안내");
        map.put("location","스터디룸");map.put("code",code);return map;
    }
    long createSession(String code) throws Exception {
        var payload=sessionBody(code);payload.put("createdBy",b.id());
        var result=mvc.perform(post("/api/sessions").with(user(admin)).with(csrf()).contentType("application/json").content(json(payload)))
                .andExpect(status().isCreated()).andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.startsAt").value("2026-11-20T05:00:00Z")).andReturn();
        long id=((Number)JsonPath.read(result.getResponse().getContentAsString(),"$.id")).longValue();
        assertEquals(admin.id(),jdbc.queryForObject("SELECT created_by FROM study_sessions WHERE id=:id",Map.of("id",id),Long.class));
        return id;
    }
    void assign(long sid,StudyPrincipal who) throws Exception {
        mvc.perform(put("/api/sessions/{sid}/presenters/{uid}",sid,who.id()).with(user(admin)).with(csrf())).andExpect(status().isNoContent());
    }
    long createMaterial(long sid,String code) throws Exception {
        var result=mvc.perform(post("/api/sessions/{sid}/materials",sid).with(user(a)).with(csrf()).contentType("application/json")
                        .content(json(Map.of("title","영속성 발표","originalUrl","https://example.technology/slides","code",code,"authorId",b.id()))))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.author.id").value(a.id().intValue())).andReturn();
        return ((Number)JsonPath.read(result.getResponse().getContentAsString(),"$.id")).longValue();
    }
    @Test void anonymousReadsAndSecurityErrors() throws Exception {
        mvc.perform(get("/api/sessions")).andExpect(status().isOk());
        mvc.perform(get("/api/auth/me")).andExpect(status().isUnauthorized());
        mvc.perform(post("/api/auth/logout").with(csrf())).andExpect(status().isUnauthorized());
        mvc.perform(post("/api/sessions").with(csrf()).contentType("application/json").content(json(sessionBody(code())))).andExpect(status().isUnauthorized());
        mvc.perform(post("/api/sessions").with(user(admin)).contentType("application/json").content(json(sessionBody(code()))))
                .andExpect(status().isForbidden()).andExpect(jsonPath("$.code").value("INVALID_CSRF_TOKEN"));
        mvc.perform(post("/shortenUrl").with(user(admin)).with(csrf()).contentType("application/json").content("{}" )).andExpect(status().isForbidden());
        mvc.perform(get("/api/sessions/-1")).andExpect(status().isBadRequest());
        mvc.perform(get("/api/sessions").param("q","x".repeat(101))).andExpect(status().isBadRequest());
        mvc.perform(get("/study/"+code())).andExpect(status().isNotFound()).andExpect(content().contentTypeCompatibleWith("text/html"));
    }
    @Test void realLoginMaintainsSessionAndLogoutRevokesIt() throws Exception {
        var tokenResult=mvc.perform(get("/api/auth/csrf")).andExpect(status().isOk()).andReturn();
        MockHttpSession session=(MockHttpSession)tokenResult.getRequest().getSession(false);
        String token=JsonPath.read(tokenResult.getResponse().getContentAsString(),"$.token");
        var login=mvc.perform(post("/api/auth/login").session(session).header("X-CSRF-TOKEN",token)
                        .param("username",admin.loginId()).param("password",password))
                .andExpect(status().isOk()).andExpect(jsonPath("$.role").value("ADMIN")).andReturn();
        session=(MockHttpSession)login.getRequest().getSession(false);
        mvc.perform(get("/api/auth/me").session(session)).andExpect(status().isOk()).andExpect(jsonPath("$.id").value(admin.id().intValue()));
        var fresh=mvc.perform(get("/api/auth/csrf").session(session)).andReturn();
        String freshToken=JsonPath.read(fresh.getResponse().getContentAsString(),"$.token");
        mvc.perform(post("/api/auth/logout").session(session).header("X-CSRF-TOKEN",freshToken)).andExpect(status().isNoContent());
        mvc.perform(get("/api/auth/me")).andExpect(status().isUnauthorized());
        mvc.perform(post("/api/auth/login").with(csrf()).param("username",admin.loginId()).param("password","wrong"))
                .andExpect(status().isUnauthorized()).andExpect(jsonPath("$.code").value("UNAUTHORIZED"));
    }
    @Test void adminSessionUpdateAssignmentAndSearch() throws Exception {
        String code=code(); long sid=createSession(code);
        assign(sid,a);assign(sid,a);
        mvc.perform(get("/api/sessions/{id}",sid)).andExpect(jsonPath("$.presenters.length()").value(1));
        mvc.perform(get("/api/sessions").param("q","100%")).andExpect(jsonPath("$.items[0].id").value((int)sid));
        mvc.perform(get("/api/sessions").param("q",code())).andExpect(jsonPath("$.items.length()").value(0));
        var changed=sessionBody(null);changed.put("title","수정 안내");
        mvc.perform(put("/api/sessions/{id}",sid).with(user(admin)).with(csrf()).contentType("application/json").content(json(changed)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.code").value(code));
        mvc.perform(get("/api/my/sessions").with(user(a))).andExpect(jsonPath("$.items.length()").value(1));
        mvc.perform(get("/api/my/sessions").with(user(b))).andExpect(jsonPath("$.items.length()").value(0));
        mvc.perform(put("/api/sessions/{sid}/presenters/{uid}",sid,admin.id()).with(user(admin)).with(csrf()))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("INVALID_PRESENTER"));
        mvc.perform(put("/api/sessions/{id}",sid).with(user(a)).with(csrf()).contentType("application/json").content(json(changed))).andExpect(status().isForbidden());
    }
    @Test void materialOwnershipUpdateAndCurrentRedirect() throws Exception {
        long sid=createSession(code());assign(sid,a);String code=code();long mid=createMaterial(sid,code);
        mvc.perform(get("/study/{code}",code)).andExpect(status().isFound()).andExpect(header().string("Location","https://example.technology/slides"));
        String update=json(Map.of("title","수정 발표","originalUrl","https://example.com/new?week=3#part"));
        mvc.perform(put("/api/materials/{id}",mid).with(user(b)).with(csrf()).contentType("application/json").content(update)).andExpect(status().isForbidden());
        mvc.perform(put("/api/materials/{id}",mid).with(user(admin)).with(csrf()).contentType("application/json").content(update)).andExpect(status().isForbidden());
        mvc.perform(put("/api/materials/{id}",mid).with(user(a)).with(csrf()).contentType("application/json").content(update))
                .andExpect(status().isOk()).andExpect(jsonPath("$.code").value(code));
        mvc.perform(get("/study/{code}",code)).andExpect(status().isFound()).andExpect(header().string("Location","https://example.com/new?week=3#part"))
                .andExpect(header().string("Cache-Control","no-store"));
        mvc.perform(get("/api/materials/{id}",mid)).andExpect(jsonPath("$.redirectCount").value(2));
        mvc.perform(get("/api/my/materials").with(user(a))).andExpect(jsonPath("$.items.length()").value(1));
        mvc.perform(get("/api/my/materials").with(user(b))).andExpect(jsonPath("$.items.length()").value(0));
        mvc.perform(get("/api/materials").param("q","修正なし")).andExpect(jsonPath("$.items.length()").value(0));
        mvc.perform(get("/api/materials").param("q","수정").param("sessionId",Long.toString(sid))).andExpect(jsonPath("$.items.length()").value(1));
        mvc.perform(post("/api/sessions/{sid}/materials",sid).with(user(b)).with(csrf()).contentType("application/json")
                .content(json(Map.of("title","미배정","originalUrl","https://example.com")))).andExpect(status().isForbidden());
    }
    @Test void duplicateCodesAndNumbersRollbackTargets() throws Exception {
        String code=code();long sid=createSession(code);assign(sid,a);
        mvc.perform(post("/api/sessions").with(user(admin)).with(csrf()).contentType("application/json").content(json(sessionBody(code()))))
                .andExpect(status().isConflict()).andExpect(jsonPath("$.code").value("DUPLICATE_SESSION_NO"));
        number++;
        mvc.perform(post("/api/sessions").with(user(admin)).with(csrf()).contentType("application/json").content(json(sessionBody(code))))
                .andExpect(status().isConflict()).andExpect(jsonPath("$.code").value("DUPLICATE_CODE"));
        mvc.perform(post("/api/sessions/{sid}/materials",sid).with(user(a)).with(csrf()).contentType("application/json")
                .content(json(Map.of("title","충돌 자료","originalUrl","https://example.com","code",code))))
                .andExpect(status().isConflict()).andExpect(jsonPath("$.code").value("DUPLICATE_CODE"));
        assertEquals(0L,jdbc.queryForObject("SELECT COUNT(*) FROM materials WHERE session_id=:sid",Map.of("sid",sid),Long.class));
        assertEquals(1L,jdbc.queryForObject("SELECT COUNT(*) FROM study_sessions WHERE created_by=:uid",Map.of("uid",admin.id()),Long.class));
    }
    @Test void validationAndGeneratedCodes() throws Exception {
        long sid=createSession(null);assign(sid,a);
        var bad=sessionBody(code());bad.remove("location");number++;
        mvc.perform(post("/api/sessions").with(user(admin)).with(csrf()).contentType("application/json").content(json(bad))).andExpect(status().isBadRequest());
        mvc.perform(post("/api/sessions/{sid}/materials",sid).with(user(a)).with(csrf()).contentType("application/json")
                .content(json(Map.of("title","잘못된 URL","originalUrl","ftp://example.com","code",code())))).andExpect(status().isBadRequest());
        mvc.perform(post("/api/sessions/{sid}/materials",sid).with(user(a)).with(csrf()).contentType("application/json")
                .content(json(Map.of("title","자동 생성 자료","originalUrl","https://example.com"))))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.code").isString());
        mvc.perform(get("/api/materials").param("sessionId","-1")).andExpect(status().isBadRequest());
    }
    @Test void invalidSessionFieldsAreRejectedWithoutSaving() throws Exception {
        List<Map<String,Object>> inputs=new ArrayList<>();
        var blank=sessionBody(code());blank.put("title","   ");inputs.add(blank);
        var longTitle=sessionBody(code());longTitle.put("title","a".repeat(151));inputs.add(longTitle);
        var invalidDate=sessionBody(code());invalidDate.put("startsAt","not-a-date");inputs.add(invalidDate);
        var missingDate=sessionBody(code());missingDate.remove("startsAt");inputs.add(missingDate);
        var badMeeting=sessionBody(code());badMeeting.put("meetingUrl","javascript:alert(1)");inputs.add(badMeeting);
        var zero=sessionBody(code());zero.put("sessionNo",0);inputs.add(zero);
        for(var input:inputs) mvc.perform(post("/api/sessions").with(user(admin)).with(csrf())
                .contentType("application/json").content(json(input))).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_INPUT"));
        assertEquals(0L,jdbc.queryForObject("SELECT COUNT(*) FROM study_sessions WHERE created_by=:uid",Map.of("uid",admin.id()),Long.class));
    }

    @Test void materialRetriesUseNewTransactionsWithoutOrphans() throws Exception {
        String occupied=code();long sid=createSession(occupied);assign(sid,a);
        ShortCodeGenerator generator=mock(ShortCodeGenerator.class);
        String available=code();when(generator.generate()).thenReturn(occupied,available);
        var coordinator=new MaterialRegistrationService(creation,generator);
        long mid=coordinator.register(sid,a.id(),"재시도 자료","https://example.com",null);
        assertEquals(mid,((Number)JsonPath.read(mvc.perform(get("/api/materials/{id}",mid)).andReturn().getResponse().getContentAsString(),"$.id")).longValue());
        when(generator.generate()).thenReturn(occupied);
        assertThrows(LackOfShortenUrlKeyException.class,()->coordinator.register(sid,a.id(),"실패 자료","https://example.com",null));
        assertEquals(1L,jdbc.queryForObject("SELECT COUNT(*) FROM materials WHERE session_id=:sid",Map.of("sid",sid),Long.class));
    }
    @Test void concurrentCodeRegistrationCommitsOnlyOneTarget() throws Exception {
        String code=code();
        var executor=Executors.newFixedThreadPool(8);
        try {
            List<Callable<Long>> tasks=new ArrayList<>();
            for(int i=0;i<8;i++) {
                final int no=number+i;
                tasks.add(()->{
                    var session=new com.example.personal_project.domain.StudySession(null,no,"동시 등록 검증",
                            java.time.LocalDateTime.of(2026,11,20,5,0),"공지","장소",null,null,admin.id(),null,null);
                    try { return registration.register(session,code).sessionId(); }
                    catch(com.example.personal_project.global.exception.DuplicateShortenUrlCodeException e) { return null; }
                });
            }
            List<Long> winners=new ArrayList<>();
            for(var result:executor.invokeAll(tasks,30,TimeUnit.SECONDS)) {
                Long id=result.get(1,TimeUnit.SECONDS);if(id!=null)winners.add(id);
            }
            assertEquals(1,winners.size());
            assertEquals(1L,jdbc.queryForObject("SELECT COUNT(*) FROM study_sessions WHERE created_by=:uid",Map.of("uid",admin.id()),Long.class));
        } finally {executor.shutdownNow();assertTrue(executor.awaitTermination(10,TimeUnit.SECONDS));}
    }

    @Test void concurrentDbRedirectsPreserveEveryIncrement() throws Exception {
        String code=code();long sid=createSession(code);
        var executor=Executors.newFixedThreadPool(8);
        try {
            List<Callable<Void>> tasks=new ArrayList<>();
            for(int i=0;i<8;i++) tasks.add(()->{ for(int n=0;n<25;n++) assertEquals("/session.html?id="+sid,resolver.resolve(code));return null; });
            for(var result:executor.invokeAll(tasks,30,TimeUnit.SECONDS)) result.get(1,TimeUnit.SECONDS);
        } finally { executor.shutdownNow();assertTrue(executor.awaitTermination(10,TimeUnit.SECONDS)); }
        mvc.perform(get("/api/sessions/{id}",sid)).andExpect(jsonPath("$.redirectCount").value(200));
    }
}
