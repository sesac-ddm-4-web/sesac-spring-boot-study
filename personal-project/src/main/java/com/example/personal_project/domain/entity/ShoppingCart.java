package com.example.personal_project.domain.entity;

import com.example.personal_project.domain.exception.CommonConflictException;
import com.example.personal_project.presentation.dto.shoppingCart.ShoppingCartRequest;

import java.util.Objects;

public class ShoppingCart {
    public static final int MAX_CART_ITEM = 20;
    private Long collectionItemId;
    private String name;
    private Integer unitPrice;
    private Integer stock;
    private ItemType itemType;
    private Integer amount;

    public static ShoppingCart createShoppingCart(CollectionItem collectionItem,ShoppingCartRequest shoppingCartRequest){
        if(shoppingCartRequest.amount()<1 || shoppingCartRequest.amount()>5) throw new CommonConflictException("장바구니 수량은 1~5입니다");
        if(!collectionItem.isInternalItem() && collectionItem.getStock() < shoppingCartRequest.amount()) throw new CommonConflictException("재고가 부족합니다");
        return new ShoppingCart(
                collectionItem.getId(),
                collectionItem.getName(),
                collectionItem.getPrice(),
                collectionItem.getStock(),
                collectionItem.getEnumItemType(),
                shoppingCartRequest.amount());
    }

    public ShoppingCart() {
    }

    public ShoppingCart(Long collectionItemId, String name, Integer unitPrice, Integer stock, ItemType itemType, Integer amount) {
        this.collectionItemId = collectionItemId;
        this.name = name;
        this.unitPrice = unitPrice;
        this.stock = stock;
        this.itemType = itemType;
        this.amount = amount;
    }

    public Boolean sameCollectionItemId(Long collectionItemId){
        return this.collectionItemId.equals(collectionItemId);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        ShoppingCart that = (ShoppingCart) o;
        return Objects.equals(collectionItemId, that.collectionItemId);
    }

    public Long getCollectionItemId() {
        return collectionItemId;
    }

    public Integer getAmount() {
        return amount;
    }

    public String getName() {
        return name;
    }

    public Integer getUnitPrice() {
        return unitPrice;
    }

    public Integer getStock() {
        return stock;
    }

    public String getItemType() {
        return itemType.name();
    }

    public void setCollectionItemId(Long collectionItemId) {
        this.collectionItemId = collectionItemId;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setUnitPrice(Integer unitPrice) {
        this.unitPrice = unitPrice;
    }

    public void setStock(Integer stock) {
        this.stock = stock;
    }

    public void setItemType(String itemType) {
        this.itemType = ItemType.valueOf(itemType);
    }

    public void setAmount(Integer amount) {
        this.amount = amount;
    }
}
