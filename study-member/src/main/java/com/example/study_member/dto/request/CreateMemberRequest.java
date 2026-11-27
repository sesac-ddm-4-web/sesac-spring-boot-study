package com.example.study_member.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateMemberRequest(
        @NotBlank
        @Size(max = 255)
        String name,
        String description) {
}
