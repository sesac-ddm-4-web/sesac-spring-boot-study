package com.example.study_member.controller;

import com.example.study_member.dto.request.CreateMemberRequest;
import com.example.study_member.dto.request.UpdateMemberProfileRequest;
import com.example.study_member.dto.response.MemberDetailResponse;
import com.example.study_member.service.MemberService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/member")
public class MemberController {
    private final MemberService memberService;

    public MemberController(MemberService memberService) {
        this.memberService = memberService;
    }

    @PostMapping
    public ResponseEntity<MemberDetailResponse> createMember(@Valid @RequestBody CreateMemberRequest request) {
        MemberDetailResponse response = memberService.createMember(request);
        return ResponseEntity
                .created(URI.create("/member/%d".formatted(response.memberId())))
                .body(response);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<MemberDetailResponse> updateMemberProfile(@PathVariable Long id, @RequestBody UpdateMemberProfileRequest request) {
        MemberDetailResponse response = memberService.updateMemberProfile(id, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<MemberDetailResponse> getMemberDetail(@PathVariable Long id) {
        MemberDetailResponse response = memberService.getMemberDetail(id);
        return ResponseEntity.ok(response);
    }
}
