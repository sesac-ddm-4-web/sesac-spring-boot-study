package com.example.studyservice.member.controller;

import com.example.studyservice.member.dto.req.ProfileUpdateReq;
import com.example.studyservice.member.dto.res.MemberProfileRes;
import com.example.studyservice.member.dto.res.MemberRes;
import com.example.studyservice.member.dto.res.ProfileRes;
import com.example.studyservice.member.service.MemberService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/members")
public class MemberController {

    private final MemberService memberService;

    public MemberController(MemberService memberService) {
        this.memberService = memberService;
    }

    @GetMapping("/me")
    public MemberRes findMe(@AuthenticationPrincipal Long memberId) {
        return memberService.findMe(memberId);
    }

    @PutMapping("/me/profile")
    public ProfileRes updateMyProfile(@AuthenticationPrincipal Long memberId,
                                      @Valid @RequestBody ProfileUpdateReq request) {
        return memberService.updateProfile(memberId, request);
    }

    @GetMapping("/{memberId}")
    public MemberProfileRes findMemberProfile(@PathVariable Long memberId) {
        return memberService.findProfileOf(memberId);
    }
}
