package com.example.wishlist.management.domain.wish;

import java.util.List;

public interface WishRepository {

    // 등록 (가격 오름차순 정렬, 같은 가격은 등록 순서 유지)
    Wish add(Wish wish);

    // 전체 목록 조회
    List<Wish> getWishList();

    // id로 단건 조회
    Wish getWish(long id);

    // id 기반 수정
    Wish updateWish(Wish wish);

    // id 기반 삭제
    void deleteWish(long id);

    // 예산 이하 아이템 최대 3개 추천
    List<Wish> getRecommendWishList(long budget);

    // 새롭게 추가
    boolean existByExternalId(long externalId);
}