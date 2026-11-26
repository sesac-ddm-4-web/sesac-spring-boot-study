package com.example.personal_project.presentation.dto.study;

import com.example.personal_project.application.study.command.SessionCommand;
import jakarta.validation.constraints.*;
import java.time.OffsetDateTime;

public record SessionInput(@Min(1) int sessionNo, @NotBlank @Size(max=150) String title,
        @NotNull OffsetDateTime startsAt, @NotBlank @Size(max=5000) String notice, @Size(max=200) String location,
        @Size(max=2048) String meetingUrl, @Size(max=2000) String preparation, String code) {
    public SessionCommand toCommand() { return new SessionCommand(sessionNo, title, startsAt, notice, location, meetingUrl, preparation, code); }
}
