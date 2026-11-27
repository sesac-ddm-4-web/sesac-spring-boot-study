package com.example.personal_project.study.api;

import com.example.personal_project.common.InvalidDomainFieldException;
import com.example.personal_project.common.api.ApiRequest;
import com.example.personal_project.common.api.PageResponse;
import com.example.personal_project.study.application.StudyMeetingService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.time.temporal.ChronoField;
import java.util.Locale;
import java.util.Map;

@RestController
@RequestMapping(StudyController.PATH + "/{studyId}/meetings")
public class StudyMeetingController {

    private static final DateTimeFormatter MEETING_TIME = new DateTimeFormatterBuilder()
            .parseCaseInsensitive()
            .append(DateTimeFormatter.ISO_LOCAL_DATE)
            .appendLiteral('T')
            .appendPattern("HH:mm")
            .optionalStart()
            .appendPattern(":ss")
            .appendFraction(ChronoField.NANO_OF_SECOND, 0, 6, true)
            .optionalEnd()
            .appendOffsetId()
            .toFormatter(Locale.ROOT)
            .withResolverStyle(ResolverStyle.STRICT);

    private final StudyMeetingService meetingService;

    public StudyMeetingController(StudyMeetingService meetingService) {
        this.meetingService = meetingService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public StudyMeetingService.Meeting create(
            @PathVariable("studyId") long studyId,
            @RequestHeader(name = ApiRequest.MEMBER_HEADER, required = false) String actor,
            @RequestBody Map<String, Object> body) {
        return meetingService.create(ApiRequest.positiveId(studyId, "studyId"), ApiRequest.actorId(actor),
                ApiRequest.text(body, "title"), ApiRequest.text(body, "location"),
                meetingTime(body, "startsAt"), meetingTime(body, "endsAt"));
    }

    @GetMapping
    public PageResponse<StudyMeetingService.Meeting> list(
            @PathVariable("studyId") long studyId,
            @RequestHeader(name = ApiRequest.MEMBER_HEADER, required = false) String actor,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size) {
        return PageResponse.from(meetingService.list(ApiRequest.positiveId(studyId, "studyId"),
                ApiRequest.actorId(actor), ApiRequest.page(page, size)));
    }

    private static Instant meetingTime(Map<String, Object> body, String field) {
        String value = ApiRequest.text(body, field);
        if (value == null) {
            throw new InvalidDomainFieldException(field, field + " is required");
        }
        try {
            return OffsetDateTime.parse(value, MEETING_TIME).toInstant();
        } catch (DateTimeParseException exception) {
            throw new InvalidDomainFieldException(field, field + " must be an ISO 8601 time with an offset and at most six fractional digits");
        }
    }
}
