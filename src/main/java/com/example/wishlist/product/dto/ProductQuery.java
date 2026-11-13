package com.example.wishlist.product.dto;

import com.example.wishlist.common.exception.InvalidRequestException;

// 외부 상품 목록, 검색 요청 조건
public record ProductQuery(String keyword, int limit, int skip) {

    public static final int DEFAULT_LIMIT = 10;
    public static final int MIN_LIMIT = 1;
    public static final int MAX_LIMIT = 20;
    public static final int DEFAULT_SKIP = 0;

    public static ProductQuery forList(Integer limit, Integer skip) {
        return new ProductQuery(null, resolveLimit(limit), resolveSkip(skip));
    }

    public static ProductQuery forSearch(String q, Integer limit, Integer skip) {
        if (q == null || q.isBlank()) {
            throw new InvalidRequestException("검색어를 입력해주세요.");
        }
        return new ProductQuery(q.strip(), resolveLimit(limit), resolveSkip(skip));
    }

    private static int resolveLimit(Integer limit) {
        if (limit == null) return DEFAULT_LIMIT;
        if (limit < MIN_LIMIT || limit > MAX_LIMIT) {
            throw new InvalidRequestException(
                MIN_LIMIT + "~" + MAX_LIMIT + " 사이여야 합니다. limit=" + limit);
        }
        return limit;
    }

    private static int resolveSkip(Integer skip) {
        if (skip == null) {
            return DEFAULT_SKIP;
        }
        if (skip < 0) {
            throw new InvalidRequestException("skip은 0 이상이어야 합니다. skip=" + skip);
        }
        return skip;
    }
}
