package com.example.personal_project.domain.study.model;



public record Member(Long id, String loginId, String passwordHash, String displayName, MemberRole role) {}
