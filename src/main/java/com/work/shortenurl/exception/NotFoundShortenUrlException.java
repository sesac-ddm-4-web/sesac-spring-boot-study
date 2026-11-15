package com.work.shortenurl.exception;

public class NotFoundShortenUrlException extends BusinessException {
    public NotFoundShortenUrlException(String shortenUrlKey) {
        super(ErrorCode.SHORTEN_URL_NOT_FOUND, "단축 URL을 찾을 수 없습니다. key=" + shortenUrlKey);
    }
}