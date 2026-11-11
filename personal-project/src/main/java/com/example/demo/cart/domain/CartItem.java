package com.example.demo.cart.domain;

import com.example.demo.global.exception.ConflictException;
import com.example.demo.item.domain.CollectionItem;

// 장바구니에 담을 때 상품을 복사해서 담음 -> 그래야 원본이 수정되지 않음
public class CartItem {

    private static final int MAX_QUANTITY = 5;

    private final Long id;
    private final Long itemId; // 원본 item id
    private final String name;
    private final int unitPrice;
    private final Integer stock; // 수동은 null
    private int quantity;

    private CartItem(Long id, Long itemId, String name, int unitPrice, Integer stock, int quantity) {
        this.id = id;
        this.itemId = itemId;
        this.name = name;
        this.unitPrice = unitPrice;
        this.stock = stock;
        this.quantity = quantity;
    }

    public static CartItem create(Long id, CollectionItem item, int quantity) {
        CartItem cartItem = new CartItem(
                id, item.getId(), item.getName(), item.getPrice(), item.getStock(), quantity);

        if (cartItem.stock != null && cartItem.stock == 0) {
            throw new ConflictException("품절된 상품은 담을 수 없습니다.");
        }
        cartItem.checkQuantity(quantity);
        return cartItem;
    }

    // 같은 상품 더할 때
    public void addQuantity(int amount) {
        int newQuantity = this.quantity + amount;
        checkQuantity(newQuantity);
        this.quantity = newQuantity;
    }

    public void changeQuantity(int newQuantity) {
        checkQuantity(newQuantity);
        this.quantity = newQuantity;
    }

    private void checkQuantity(int newQuantity) {
        if (newQuantity > MAX_QUANTITY) {
            throw new ConflictException("한 상품은 최대 " + MAX_QUANTITY + "개까지 담을 수 있습니다.");
        }
        if (stock != null && newQuantity > stock) {
            throw new ConflictException("재고(" + stock + "개)를 초과할 수 없습니다.");
        }
    }

    public long lineTotal() {
        return (long) unitPrice * quantity;
    }

    public boolean isSameItem(Long itemId) {
        return this.itemId.equals(itemId);
    }

    public boolean hasId(Long id) {
        return this.id.equals(id);
    }

    public Long getId() { return id; }
    public Long getItemId() { return itemId; }
    public String getName() { return name; }
    public int getUnitPrice() { return unitPrice; }
    public Integer getStock() { return stock; }
    public int getQuantity() { return quantity; }
}
