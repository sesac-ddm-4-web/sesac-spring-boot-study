package com.example.wishlistbudget.repository;

import com.example.wishlistbudget.domain.CollectionItem;
import com.example.wishlistbudget.domain.SourceType;

import java.util.List;
import java.util.Optional;

public interface CollectionRepository {

    CollectionItem save(String name, int price);

    List<CollectionItem> findAll();

    Optional<CollectionItem> findById(Long id);

    // 외부 상품 저장
    CollectionItem saveExternal(
            String name,
            int price,
            Long externalId,
            Integer stock
    );

    // 중복 확인
    boolean existsBySourceTypeAndExternalId(
            SourceType sourceType,
            Long externalId
    );
}