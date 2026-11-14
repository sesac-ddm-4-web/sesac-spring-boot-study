
package com.example.campkit.domain;

import com.example.campkit.exception.ConflictException;

public class CartItem {

    private final long id;
    private final long collectionItemId;
    private final String name;
    private final long unitPrice;
    private final Integer stock;
    private final int quantity;

    public CartItem(long id, long collectionItemId,
                    String name, long unitPrice,
                    Integer stock, int quantity) {

        validateQuantity(quantity);
        validateStock(stock, quantity);

        this.id = id;
        this.collectionItemId = collectionItemId;
        this.name = name;
        this.unitPrice = unitPrice;
        this.stock = stock;
        this.quantity = quantity;
    }

    private static void validateQuantity(int quantity) {
        if (quantity < 1 || quantity > 5) {
            throw new ConflictException(
                    "수량은 1~5개여야 합니다."
            );
        }
    }

    private static void validateStock(Integer stock, int quantity) {
        if (stock != null && quantity > stock) {
            throw new ConflictException(
                    "상품 재고가 부족합니다."
            );
        }
    }

    public CartItem withId(long id) {
        return new CartItem(
                id, collectionItemId, name,
                unitPrice, stock, quantity
        );
    }

    public CartItem withQuantity(int quantity) {
        return new CartItem(
                id, collectionItemId, name,
                unitPrice, stock, quantity
        );
    }

    public CartItem addQuantity(int quantity) {
        return withQuantity(
                Math.addExact(this.quantity, quantity)
        );
    }

    public long getLineAmount() {
        return Math.multiplyExact(unitPrice, quantity);
    }

    public long getId() { return id; }
    public long getCollectionItemId() { return collectionItemId; }
    public String getName() { return name; }
    public long getUnitPrice() { return unitPrice; }
    public Integer getStock() { return stock; }
    public int getQuantity() { return quantity; }
}

