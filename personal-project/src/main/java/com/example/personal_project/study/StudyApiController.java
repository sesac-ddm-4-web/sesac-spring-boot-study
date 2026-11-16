package com.example.personal_project.study;

import com.example.personal_project.domain.*;
import com.example.personal_project.application.StudySessionRegistrationService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import java.net.URI;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.*;

@RestController
@RequestMapping("/api")
public class StudyApiController {
    private final StudyStore store;
    private final StudySessionRepository sessions;
    private final StudySessionRegistrationService registration;
    private final MaterialRegistrationService materialRegistration;
    private final String base;
    public StudyApiController(StudyStore store, StudySessionRepository sessions,
            StudySessionRegistrationService registration,MaterialRegistrationService materialRegistration,
            @Value("${app.base-url:http://localhost:8080}") String base) {
        this.store=store; this.sessions=sessions; this.registration=registration;
        this.materialRegistration=materialRegistration; this.base=base.replaceAll("/+$","");
    }
    public record SessionInput(@Min(1) int sessionNo,@NotBlank @Size(max=150) String title,
            @NotNull OffsetDateTime startsAt,@NotBlank @Size(max=5000) String notice,
            @Size(max=200) String location,@Size(max=2048) String meetingUrl,
            @Size(max=2000) String preparation,String code) {}
    public record MaterialInput(@NotBlank @Size(max=150) String title,@NotBlank @Size(max=2048) String originalUrl,String code) {}
    public record MaterialUpdate(@NotBlank @Size(max=150) String title,@NotBlank @Size(max=2048) String originalUrl) {}

    private StudySession sessionInput(SessionInput input,Long id,Long creator) {
        String location=StudyValidation.optional(input.location(),200);
        String meeting=StudyValidation.optional(input.meetingUrl(),2048);
        if(location==null && meeting==null) throw new IllegalArgumentException("장소 또는 온라인 접속 링크를 입력해주세요.");
        if(meeting!=null) StudyValidation.url(meeting);
        return new StudySession(id,input.sessionNo(),StudyValidation.text(input.title(),150,"회차 제목"),
                input.startsAt().withOffsetSameInstant(ZoneOffset.UTC).toLocalDateTime(),
                StudyValidation.text(input.notice(),5000,"공지"),location,meeting,
                StudyValidation.optional(input.preparation(),2000),creator,null,null);
    }
    private StudySession requireSession(long id) {
        StudyValidation.positive(id); var s=sessions.findById(id);
        if(s==null) throw StudyException.missing(); return s;
    }
    @GetMapping("/sessions") public Map<String,Object> sessions(@RequestParam(defaultValue="") String q) {
        return Map.of("items",store.sessions(StudyValidation.query(q),null,base));
    }
    @GetMapping("/sessions/{id}") public Map<String,Object> session(@PathVariable long id) {
        var s=requireSession(id); var link=store.sessionLink(id);
        var result=new LinkedHashMap<String,Object>();
        result.put("id",s.id()); result.put("sessionNo",s.sessionNo()); result.put("title",s.title());
        result.put("startsAt",s.startsAt().atOffset(ZoneOffset.UTC)); result.put("notice",s.notice());
        result.put("location",s.location()); result.put("meetingUrl",s.meetingUrl()); result.put("preparation",s.preparation());
        result.put("code",link.get("code")); result.put("shortUrl",base+"/study/"+link.get("code"));
        result.put("redirectCount",link.get("redirectCount")); result.put("presenters",store.sessionPresenters(id));
        result.put("materials",store.materials("",id,null,null,base)); return result;
    }
    @PostMapping("/sessions") public ResponseEntity<Map<String,Object>> createSession(
            @Valid @RequestBody SessionInput input,@AuthenticationPrincipal StudyPrincipal user) {
        var result=registration.register(sessionInput(input,null,user.id()),input.code());
        return ResponseEntity.created(URI.create("/api/sessions/"+result.sessionId())).body(session(result.sessionId()));
    }
    @PutMapping("/sessions/{id}") public Map<String,Object> updateSession(@PathVariable long id,
            @Valid @RequestBody SessionInput input) {
        var existing=requireSession(id);
        if(input.code()!=null) throw new IllegalArgumentException("생성된 단축 코드는 변경할 수 없습니다.");
        store.updateSession(sessionInput(input,id,existing.createdBy())); return session(id);
    }
    @GetMapping("/presenters") public Map<String,Object> presenters() { return Map.of("items",store.presenters()); }
    @PutMapping("/sessions/{sid}/presenters/{uid}") public ResponseEntity<Void> assign(@PathVariable long sid,@PathVariable long uid) {
        requireSession(sid); StudyValidation.positive(uid); var member=store.member(uid);
        if(member==null) throw StudyException.missing();
        if(!member.role().equals("PRESENTER")) throw new StudyException(400,"INVALID_PRESENTER","발표자 계정만 배정할 수 있습니다.");
        store.assign(sid,uid); return ResponseEntity.noContent().build();
    }
    @GetMapping("/materials") public Map<String,Object> materials(@RequestParam(defaultValue="") String q,
            @RequestParam(required=false) Long sessionId) {
        if(sessionId!=null) StudyValidation.positive(sessionId);
        return Map.of("items",store.materials(StudyValidation.query(q),sessionId,null,null,base));
    }
    @GetMapping("/materials/{id}") public Map<String,Object> material(@PathVariable long id) {
        StudyValidation.positive(id); var rows=store.materials("",null,null,id,base);
        if(rows.isEmpty()) throw StudyException.missing(); return rows.getFirst();
    }
    @PostMapping("/sessions/{sid}/materials") public ResponseEntity<Map<String,Object>> createMaterial(
            @PathVariable long sid,@Valid @RequestBody MaterialInput input,@AuthenticationPrincipal StudyPrincipal user) {
        StudyValidation.positive(sid);
        long id=materialRegistration.register(sid,user.id(),input.title(),input.originalUrl(),input.code());
        return ResponseEntity.created(URI.create("/api/materials/"+id)).body(material(id));
    }
    @PutMapping("/materials/{id}") @Transactional
    public Map<String,Object> updateMaterial(@PathVariable long id,@Valid @RequestBody MaterialUpdate input,
            @AuthenticationPrincipal StudyPrincipal user) {
        StudyValidation.positive(id); var existing=store.material(id);
        if(existing==null) throw StudyException.missing();
        if(!existing.authorId().equals(user.id())) throw StudyException.forbidden();
        store.updateMaterial(id,StudyValidation.text(input.title(),150,"자료 제목"),StudyValidation.url(input.originalUrl()));
        return material(id);
    }
    @GetMapping("/my/sessions") public Map<String,Object> mySessions(@AuthenticationPrincipal StudyPrincipal user) {
        return Map.of("items",store.sessions("",user.id(),base));
    }
    @GetMapping("/my/materials") public Map<String,Object> myMaterials(@AuthenticationPrincipal StudyPrincipal user) {
        return Map.of("items",store.materials("",null,user.id(),null,base));
    }
}
