package com.example.wishlist.item.dto;

import com.example.wishlist.common.exception.InvalidRequestException;

public record ItemImportRequest(Long externalId) {

    public long validatedExternalId() {
        if (externalId == null) {
            throw new InvalidRequestException("externalId는 필수입니다.");
        }
        if (externalId <= 0) {
            throw new InvalidRequestException("externalId는 1 이상이어야 합니다.");
        }
        return externalId;
    }
}