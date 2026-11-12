package com.example.sesac_spring_boot_study.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

public class Cart {
    private static final int MIN_QUANTITY = 1;
    private static final int MAX_QUANTITY = 5;
    private static final int MAX_KINDS = 20;

    private final List<CartItem> items = new ArrayList<>();
    private long nextRowId = 1;

    private int rangeCheck (int quantity) {
        if (quantity < MIN_QUANTITY || quantity > MAX_QUANTITY) {
            throw new IllegalArgumentException("수량은 1~5여야 합니다.");
        }
        return quantity;
    }

    private void limitCheck(int totalQuantity, Integer stock) {
        if (totalQuantity > MAX_QUANTITY) {
            throw new IllegalStateException("한 상품은 최대 5개까지 담을 수 있습니다.");
        }
        if (stock != null && totalQuantity > stock) {
            throw new IllegalStateException("재고가 부족합니다.");
        }
    }

    public CartItem add(Long productId, String name, long price, Integer stock, int quantity){
        rangeCheck(quantity);
        CartItem item = findByProductId(productId);
        if (item != null){
            int newQuantity = item.getQuantity()+quantity;
            limitCheck(newQuantity, item.getStock());
            item.setQuantity(newQuantity);
            return item;
        }

        if (items.size() >= MAX_KINDS){
            throw new IllegalStateException("장바구니에는 최대 20종까지 담을 수 있습니다.");
        }

        limitCheck(quantity, stock);

        CartItem itemReturn = new CartItem(nextRowId++, productId, name, price, stock, quantity);
        items.add(itemReturn);
        return itemReturn;

    }

    public CartItem changeQuantity(Long rowId, int quantity) {
        rangeCheck(quantity);
        CartItem item = findByRowId(rowId);
        limitCheck(quantity, item.getStock());
        item.setQuantity(quantity);
        return item;
    }

    public void remove(Long rowId) {
        CartItem item = findByRowId(rowId);
        items.remove(item);
    }
    public List<CartItem> getItems() {
        return new ArrayList<>(items);
    }

    public long subtotal() {
        long sum = 0;
        for (CartItem item : items) {
            sum += item.total();
        }
        return sum;
    }

    public boolean isEmpty() {
        return items.isEmpty();
    }

    private CartItem findByRowId(Long rowId) {
        return items.stream()
                .filter(item -> item.getRowId().equals(rowId))
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException("장바구니 항목을 찾을 수 없습니다."));
    }

    private CartItem findByProductId(Long productId) {
        return items.stream()
                .filter(item -> item.getProductId().equals(productId))
                .findFirst()
                .orElse(null);
    }

    private static final long FREE_SHIPPING_THRESHOLD = 100_000;
    private static final long SHIPPING_FEE = 5_000;

    public CartPreview preview(Coupon coupon) {
        if (items.isEmpty()) {
            if (coupon != Coupon.NONE) {
                throw new IllegalArgumentException("빈 장바구니에는 쿠폰을 사용할 수 없습니다.");
            }
            return new CartPreview(0, 0, 0, 0, 0, Coupon.NONE);   // 배송비도 0
        }

        long subtotal = subtotal();
        long discount = coupon.discount(subtotal);
        long discountedSubtotal = subtotal - discount;
        long shipping = discountedSubtotal >= FREE_SHIPPING_THRESHOLD ? 0 : SHIPPING_FEE;
        long finalAmount = discountedSubtotal + shipping;

        return new CartPreview(subtotal, discount, discountedSubtotal, shipping, finalAmount, coupon);
    }




}
