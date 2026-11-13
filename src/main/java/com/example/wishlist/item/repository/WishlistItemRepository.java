package com.example.wishlist.item.repository;

import com.example.wishlist.item.domain.WishlistItem;
import java.util.List;
import java.util.Optional;

public interface WishlistItemRepository {
    // item 저장
    WishlistItem save(WishlistItem item);

    // 수정
    WishlistItem update(WishlistItem item);

    // 삭제
    boolean deleteById(Long id);

    // 전체 목록
    List<WishlistItem> findAll();

    // id로 item 찾기
    Optional<WishlistItem> findById(Long id);

    // 외부 상품 중복 가져오는지 확인
    Optional<WishlistItem> findByExternalId(Long externalId);
}
