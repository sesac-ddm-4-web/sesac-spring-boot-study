package com.example.wishlist.cart.domain;

import com.example.wishlist.cart.exception.CartRuleViolationException;
import com.example.wishlist.cart.exception.InvalidQuantityException;
import com.example.wishlist.item.domain.WishlistItem;

public final class CartItem {

    public static final int MIN_QUANTITY = 1;
    public static final int MAX_QUANTITY = 5;

    private final Long id;              // 장바구니 행 id (위시리스트 id와 별개)
    private final Long wishlistItemId;  // 어떤 위시리스트 아이템에서 담았는지
    private final String name;          // 복사한 이름
    private final long unitPrice;       // 복사한 단가 (금액 계산은 long)
    private final int quantity;
    private final Integer stockLimit;   // 복사한 재고 (null = 직접 등록 상품, 제한 없음)

    private CartItem(Long id, Long wishlistItemId, String name, long unitPrice, int quantity, Integer stockLimit) {
        this.id = id;
        this.wishlistItemId = wishlistItemId;
        this.name = name;
        this.unitPrice = unitPrice;
        this.quantity = quantity;
        this.stockLimit = stockLimit;
    }

    // 처음 담을 때 - 위시리스트 아이템 복사해서 만든다
    static CartItem snapshotOf(long id, WishlistItem item, int quantity) {
        validateQuantity(quantity);
        CartItem created = new CartItem(
            id, item.getId(), item.getName(), item.getPrice(), quantity, item.getStock());
        created.checkStock(quantity);
        return created;
    }

    // 같은 상품 또 담응ㄹ 때 - 수량 더하기 (최대 5개, 복사한 재고 이하)
    CartItem addQuantity(int added) {
        validateQuantity(added);
        int merged = quantity + added;
        if (merged > MAX_QUANTITY) {
            throw new CartRuleViolationException(String.format(
                "한 상품은 최대 %d개까지 담을 수 있습니다. (현재 %d개 + 추가 %d개)",
                MAX_QUANTITY, quantity, added));
        }
        checkStock(merged);
        return withQuantity(merged);
    }

    // 수량 변경
    CartItem changeQuantity(int newQuantity) {
        validateQuantity(newQuantity);
        checkStock(newQuantity);
        return withQuantity(newQuantity);
    }

    public long lineTotal() {
        return Math.multiplyExact(unitPrice, quantity);
    }

    static void validateQuantity(int quantity) {
        if (quantity < MIN_QUANTITY || quantity > MAX_QUANTITY) {
            throw new InvalidQuantityException(quantity);
        }
    }

    void checkStock(int requested) {
        if (stockLimit != null && requested > stockLimit) {
            throw new CartRuleViolationException(String.format(
                "재고(%d개)보다 많이 담을 수 없습니다. (요청 %d개, %s)", stockLimit, requested, name));
        }
    }

    private CartItem withQuantity(int newQuantity) {
        return new CartItem(id, wishlistItemId, name, unitPrice, newQuantity, stockLimit);
    }

    public Long getId() {
        return id;
    }

    public Long getWishlistItemId() {
        return wishlistItemId;
    }

    public String getName() {
        return name;
    }

    public long getUnitPrice() {
        return unitPrice;
    }

    public int getQuantity() {
        return quantity;
    }

    public Integer getStockLimit() {
        return stockLimit;
    }

    @Override
    public String toString() {
        return "CartItem{id=" + id + ", wishlistItemId=" + wishlistItemId + ", name='" + name
            + "', unitPrice=" + unitPrice + ", quantity=" + quantity + ", stockLimit=" + stockLimit + "}";
    }

    // DB -> 도메인
    public static CartItem restore(Long id, Long wishlistItemId, String name,
        long unitPrice, int quantity, Integer stockLimit) {
        return new CartItem(id, wishlistItemId, name, unitPrice, quantity, stockLimit);
    }
}
