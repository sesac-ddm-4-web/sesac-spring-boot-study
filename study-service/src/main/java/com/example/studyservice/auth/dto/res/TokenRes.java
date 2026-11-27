package com.example.studyservice.auth.dto.res;

public record TokenRes(
        String tokenType,
        String accessToken,
        long accessTokenExpiresIn,
        String refreshToken
) {
    public static TokenRes bearer(String accessToken, long accessTokenExpiresIn, String refreshToken) {
        return new TokenRes("Bearer", accessToken, accessTokenExpiresIn, refreshToken);
    }
}
