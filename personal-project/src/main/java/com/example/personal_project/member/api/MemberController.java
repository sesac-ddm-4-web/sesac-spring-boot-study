package com.example.personal_project.member.api;

import com.example.personal_project.common.api.ApiRequest;
import com.example.personal_project.member.application.MemberService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.Map;

@RestController
@RequestMapping(MemberController.PATH)
public class MemberController {

    static final String PATH = "/api/v1/members";

    private final MemberService memberService;

    public MemberController(MemberService memberService) {
        this.memberService = memberService;
    }

    @PostMapping
    public ResponseEntity<MemberService.Profile> register(@RequestBody Map<String, Object> request) {
        MemberService.Profile profile = memberService.register(
                ApiRequest.text(request, "email"), ApiRequest.text(request, "nickname"), ApiRequest.text(request, "bio"));
        return ResponseEntity.created(URI.create(PATH + "/" + profile.memberId())).body(profile);
    }

    @GetMapping("/{memberId}")
    public MemberService.Profile getProfile(@PathVariable("memberId") long memberId) {
        return memberService.getProfile(memberId);
    }

    @PutMapping("/me/profile")
    public MemberService.Profile updateProfile(
            @RequestHeader(name = ApiRequest.MEMBER_HEADER, required = false) String actor,
            @RequestBody Map<String, Object> request) {
        return memberService.updateProfile(ApiRequest.actorId(actor), ApiRequest.text(request, "nickname"), ApiRequest.text(request, "bio"));
    }
}
