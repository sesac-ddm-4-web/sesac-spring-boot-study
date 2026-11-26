package com.example.personal_project.presentation.dto.study;

import jakarta.validation.constraints.*;

public record MaterialInput(@NotBlank @Size(max=150) String title, @NotBlank @Size(max=2048) String originalUrl, String code) {}
