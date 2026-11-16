package com.example.personal_project.domain.study.repository;

import com.example.personal_project.domain.study.model.Member;

public interface MemberRepository { Member member(long id); Member memberByLogin(String login); }
