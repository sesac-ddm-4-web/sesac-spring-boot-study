package com.example.personal_project.domain;

import java.util.ArrayList;
import java.util.List;

//같은 상품의 수량 합산·최대 20종 제한·행 번호 관리
public class Cart {
    private final List<CartItem> items = new ArrayList<>();
    private long sequence = 1L;

    public Cart() {
    }

    // DB에서 읽은 장바구니 복원
    public Cart(List<CartItem> items, long nextSequence) {
        this.items.addAll(items);
        this.sequence = nextSequence;
    }

    public long getNextSequence() {
        return sequence;
    }

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

    public CartItem findById(Long id) {
        for (CartItem cartItem:items) {
            if (cartItem.getId().equals(id)) {
                return cartItem;
            }
        }
        throw new EntityNotFoundException(
                "장바구니 항목을 찾지 못했습니다."
        );
    }

    public CartItem changeQuantity(Long id, int quantity) {
        CartItem cartItem = findById(id);
        cartItem.changeQuantity(quantity);
        return cartItem;
    }

    public void remove(Long id) {
        CartItem cartItem = findById(id);
        items.remove(cartItem);
    }

    public CartPreview preview(Coupon coupon) {
        // 쿠폰값이 없을 때
        if (coupon == null) {
            throw new IllegalArgumentException("쿠폰을 선택해주세요.");
        }
        // 장바구니가 비어있을 때
        if (items.isEmpty()) {
            if (coupon != Coupon.NONE) {
                throw new IllegalArgumentException(
                        "빈 장바구니에는 쿠폰을 사용할 수 없습니다."
                );
            }

            return new CartPreview(0L, 0L, 0L, 0L, 0L, Coupon.NONE);
        }

        // 총 합
        long subtotal = 0L;
        for (CartItem cartItem : items) {
            subtotal += cartItem.getUnitPrice() * cartItem.getQuantity();
        }

        // 쿠폰 할인
        long discount = coupon.calculateDiscount(subtotal);
        long discountedSubtotal = subtotal - discount;

        // 배송비
        long shipping;
        if (discountedSubtotal >= 100000L) {shipping = 0L;}
        else {shipping = 5000L;}

        long finalAmount = discountedSubtotal + shipping;

        return new CartPreview(
                subtotal,
                discount,
                discountedSubtotal,
                shipping,
                finalAmount,
                coupon
        );
    }
}
