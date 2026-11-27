package com.example.study.controller;

import com.example.study.common.auth.LoginMember;
import com.example.study.common.auth.PublicApi;
import com.example.study.dto.MemberResponse;
import com.example.study.dto.MyPageResponse;
import com.example.study.dto.MyStudiesResponse;
import com.example.study.dto.SignupRequest;
import com.example.study.dto.UpdateMemberRequest;
import com.example.study.service.MemberService;
import com.example.study.service.MyPageService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/members")
public class MemberController {

    private final MemberService memberService;

    private final MyPageService myPageService;

    public MemberController(MemberService memberService, MyPageService myPageService) {
        this.memberService = memberService;
        this.myPageService = myPageService;
    }

    @PublicApi
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MemberResponse signUp(@Valid @RequestBody SignupRequest request) {
        return memberService.signUp(request);
    }

    @GetMapping("/me")
    public MyPageResponse getMe(@LoginMember Long memberId) {
        return myPageService.getMyPage(memberId);
    }

    @GetMapping("/me/studies")
    public MyStudiesResponse getMyStudies(@LoginMember Long memberId) {
        return myPageService.getMyStudies(memberId);
    }

    @PatchMapping("/me")
    public MemberResponse updateMe(@LoginMember Long memberId,
            @Valid @RequestBody UpdateMemberRequest request) {
        return memberService.updateMember(memberId, request);
    }
}
