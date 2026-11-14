package com.example.personal_project.domain;

import java.util.ArrayList;
import java.util.List;

//같은 상품의 수량 합산·최대 20종 제한·행 번호 관리
public class Cart {
    private final List<CartItem> items = new ArrayList<>();
    private long sequence = 1L;

    public CartItem add(Item item, int quantity) {
        if (quantity <1 || quantity >5) {
            throw new IllegalArgumentException( // 400
                    "추가 수량은 1개 이상 5개 이하여야 합니다."
            );
        }

        // 장바구니에 이미 있는지 조회
        for (CartItem cartItem : items) {
//            cartItem.getItemId() → 장바구니 행이 가리키는 원본 상품 번호
//            item.getId()        → 이번에 추가하려는 위시리스트 상품 번호
            if (cartItem.getItemId().equals(item.getId())) {
                int totalQuantity = cartItem.getQuantity() + quantity;
                if (totalQuantity >5) {
                    throw new IllegalStateException( // 기존 장바구니와 충돌 409
                            "같은 상품은 최대 5개까지 담을 수 있습니다."
                    );
                }
                // 합산 수량 /재고 검사 후 기존 행 수량 변경
                cartItem.changeQuantity(totalQuantity);
                return cartItem;
            }
        }

        // 서로 다른 상품은 최대 20종까지 허용
        if (items.size() >= 20) {
            throw new IllegalStateException(
                    "장바구니에는 최대 20종의 상품을 담을 수 있습니다."
            );
        }

        // 원본 상품의 값을 복사해 새 장바구니 행 생성
        CartItem newCartItem = new CartItem(
                sequence,
                item.getId(),
                item.getName(),
                item.getPrice(),
                item.getSourceType(),
                item.getStock(),
                quantity
        );

        items.add(newCartItem);
        sequence++;
        return newCartItem;
    }

    // 내부 items 목록을 복사해서 반환
    // 목록을 받은 쪽에서 Cart 내부 목록을 마음대로 바꾸지 못하게 하기위해
    public List<CartItem> getItems() {
        return new ArrayList<>(items);
    }
}
