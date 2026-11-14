package com.example.wishlist.domain;

public class CartItem {
    public static final int MIN_QUANTITY = 1;
    public static final int MAX_QUANTITY = 5;

    private Long id;
    private final Long wishItemId;
    private final String name;
    private final long unitPrice;
    private final Integer stock;
    private int quantity;

    private CartItem(Long wishItemId, String name, long unitPrice, Integer stock, int quantity) {
        this.wishItemId = wishItemId;
        this.name = name;
        this.unitPrice = unitPrice;
        this.stock = stock;
        this.quantity = quantity;
    }

    public static CartItem copyOf(CollectionItem wishItem, int quantity) {
        checkQuantityRange(quantity);

        Integer stock = wishItem.isExternal() ? wishItem.getStock() : null;
        if (stock != null && stock <= 0) {
            throw new CartConflictException("품절된 상품은 장바구니에 담을 수 없습니다.");
        }
        checkStock(stock, quantity);

        return new CartItem(wishItem.getId(), wishItem.getName(), wishItem.getPrice(), stock, quantity);
    }

    public void addQuantity(int quantity) {
        checkQuantityRange(quantity);
        int newQuantity = this.quantity + quantity;
        if (newQuantity > MAX_QUANTITY) {
            throw new CartConflictException(
                    "한 상품은 최대 5개까지 담을 수 있습니다. (현재 " + this.quantity + "개)");
        }
        checkStock(stock, newQuantity);
        this.quantity = newQuantity;
    }

    public void changeQuantity(int quantity) {
        checkQuantityRange(quantity);
        checkStock(stock, quantity);
        this.quantity = quantity;
    }

    public long getLineTotal() {
        return unitPrice * quantity;
    }

    private static void checkQuantityRange(int quantity) {
        if (quantity < MIN_QUANTITY || quantity > MAX_QUANTITY) {
            throw new IllegalArgumentException("수량은 1개부터 5개까지 가능합니다.");
        }
    }

    private static void checkStock(Integer stock, int quantity) {
        if (stock != null && quantity > stock) {
            throw new CartConflictException("재고가 부족합니다. (재고 " + stock + "개)");
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getWishItemId() {
        return wishItemId;
    }

    public String getName() {
        return name;
    }

    public long getUnitPrice() {
        return unitPrice;
    }

    public Integer getStock() {
        return stock;
    }

    public int getQuantity() {
        return quantity;
    }
}
