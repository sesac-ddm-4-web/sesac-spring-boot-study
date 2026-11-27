package com.example.personal_project.common.api;

import com.example.personal_project.common.InvalidDomainFieldException;
import com.example.personal_project.common.application.ServiceException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.Map;

public final class ApiRequest {

    public static final String MEMBER_HEADER = "X-Member-Id";

    private ApiRequest() {
    }

    public static long actorId(String header) {
        if (header == null) {
            throw new ServiceException(ServiceException.Code.ACTOR_REQUIRED);
        }
        try {
            return positiveId(Long.parseLong(header), MEMBER_HEADER);
        } catch (NumberFormatException exception) {
            throw new InvalidDomainFieldException(MEMBER_HEADER, "X-Member-Id must be a positive integer");
        }
    }

    public static long positiveId(long id, String field) {
        if (id <= 0) {
            throw new InvalidDomainFieldException(field, field + " must be a positive integer");
        }
        return id;
    }

    public static String text(Map<String, Object> body, String field) {
        Object value = body.get(field);
        if (value != null && !(value instanceof String)) {
            throw new InvalidDomainFieldException(field, field + " must be a string");
        }
        return (String) value;
    }

    public static int integer(Map<String, Object> body, String field) {
        if (body.get(field) instanceof Integer value) {
            return value;
        }
        throw new InvalidDomainFieldException(field, field + " must be an integer");
    }

    public static Pageable page(int page, int size) {
        if (page < 0) {
            throw new InvalidDomainFieldException("page", "page must be zero or greater");
        }
        if (size < 1 || size > 100) {
            throw new InvalidDomainFieldException("size", "size must be between 1 and 100");
        }
        if ((long) page * size > Integer.MAX_VALUE) {
            throw new InvalidDomainFieldException("page", "page is outside the supported range for this size");
        }
        return PageRequest.of(page, size);
    }
}
