package com.example.wishlistbudget.repository;

import com.example.wishlistbudget.domain.CartItem;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class MemoryCartRepository implements CartRepository {

    private final List<CartItem> items = new ArrayList<>();

    private long sequence = 0L;

    // 새로운 장바구니 상품 저장
    @Override
    public synchronized CartItem save(
            Long collectionItemId,
            String name,
            int price,
            Integer stock,
            int quantity
    ) {
        sequence++;

        CartItem item = new CartItem(
                sequence,
                collectionItemId,
                name,
                price,
                stock,
                quantity
        );

        items.add(item);

        return item;
    }

    // 장바구니 ID로 조회
    @Override
    public synchronized Optional<CartItem> findById(Long id) {

        for (CartItem item : items) {
            if (item.id().equals(id)) {
                return Optional.of(item);
            }
        }

        return Optional.empty();
    }

    // 위시리스트 상품 ID로 조회
    @Override
    public synchronized Optional<CartItem> findByCollectionItemId(
            Long collectionItemId
    ) {

        for (CartItem item : items) {
            if (item.collectionItemId().equals(collectionItemId)) {
                return Optional.of(item);
            }
        }

        return Optional.empty();
    }

    // 장바구니 전체 조회
    @Override
    public synchronized List<CartItem> findAll() {
        return new ArrayList<>(items);
    }

    // 기존 장바구니 상품 수정
    @Override
    public synchronized CartItem update(CartItem updatedItem) {

        for (CartItem item : items) {

            if (item.id().equals(updatedItem.id())) {

                int index = items.indexOf(item);

                items.set(index, updatedItem);

                return updatedItem;
            }
        }

        throw new IllegalStateException(
                "수정할 장바구니 상품이 존재하지 않습니다."
        );
    }

    // 장바구니 상품 삭제
    @Override
    public synchronized boolean deleteById(Long id) {

        for (CartItem item : items) {

            if (item.id().equals(id)) {

                items.remove(item);

                return true;
            }
        }

        return false;
    }
}