package com.sesac.studyhub.member.controller;

import com.sesac.studyhub.member.dto.MemberProfileResponse;
import com.sesac.studyhub.member.dto.MyInfoResponse;
import com.sesac.studyhub.member.dto.ProfileUpdateRequest;
import com.sesac.studyhub.member.service.MemberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService service;

    @GetMapping("/me")
    public ResponseEntity<MyInfoResponse> getMyInfo(@AuthenticationPrincipal Long memberId) {
        return ResponseEntity.ok(service.getMyInfo(memberId));
    }

    @PatchMapping("/me/profile")
    public ResponseEntity<MyInfoResponse> updateMyProfile(@AuthenticationPrincipal Long memberId,
        @Valid @RequestBody ProfileUpdateRequest request) {
        return ResponseEntity.ok(service.updateMyProfile(memberId, request));
    }

    @GetMapping("/{memberId}")
    public ResponseEntity<MemberProfileResponse> getMember(@PathVariable Long memberId) {
        return ResponseEntity.ok(service.getMemberProfile(memberId));
    }
}
