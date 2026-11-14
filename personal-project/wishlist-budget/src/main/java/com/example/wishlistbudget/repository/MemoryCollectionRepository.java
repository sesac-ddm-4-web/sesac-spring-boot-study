package com.example.wishlistbudget.repository;

import com.example.wishlistbudget.domain.CollectionItem;
import com.example.wishlistbudget.domain.SourceType;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class MemoryCollectionRepository implements CollectionRepository {

    private final List<CollectionItem> items = new ArrayList<>();

    private long sequence = 0L;

    // 아이템 저장
    @Override
    public CollectionItem save(String name, int price) {

        sequence++;

        CollectionItem item = new CollectionItem(
                sequence,
                name,
                price,
                SourceType.MANUAL,
                null,
                null
        );

        items.add(item);

        return item;
    }

    // 전체 아이템 조회
    @Override
    public List<CollectionItem> findAll() {
        return new ArrayList<>(items);
    }

    // ID로 아이템 조회
    @Override
    public Optional<CollectionItem> findById(Long id) {

        for (CollectionItem item : items) {

            if (item.id().equals(id)) {
                return Optional.of(item);
            }
        }

        return Optional.empty();
    }

    // 외부 상품 저장
    @Override
    public CollectionItem saveExternal(String name, int price, Long externalId, Integer stock) {

        sequence++;

        CollectionItem item = new CollectionItem(
                sequence,
                name,
                price,
                SourceType.EXTERNAL,
                externalId,
                stock
        );

        items.add(item);

        return item;
    }

    // 중복 확인
    @Override
    public boolean existsBySourceTypeAndExternalId(SourceType sourceType, Long externalId) {

        for (CollectionItem item : items) {
            if (item.sourceType() == sourceType
                    && externalId.equals(item.externalId())) {
                return true;
            }
        }
        return false;
    }
}