package com.example.personal_project.application.url.port;

import com.example.personal_project.domain.url.model.ShortLink;

public interface StudyLinkTargetPort {
    String destination(ShortLink link);
}
