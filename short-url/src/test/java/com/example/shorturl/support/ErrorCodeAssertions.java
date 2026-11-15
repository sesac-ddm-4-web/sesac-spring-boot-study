package com.example.shorturl.support;

import com.example.shorturl.global.exception.ApiException;
import com.example.shorturl.global.exception.ErrorCode;
import org.junit.jupiter.api.function.Executable;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public final class ErrorCodeAssertions {

    private ErrorCodeAssertions() {
    }

    public static void assertErrorCode(ErrorCode expected, Executable executable) {
        ApiException exception = assertThrows(ApiException.class, executable);
        assertEquals(expected, exception.getErrorCode());
    }
}
