package com.example.hobby_wishlist.domain.wishlist;

import com.example.hobby_wishlist.domain.externalproduct.ExternalProduct;

public class WishlistItem {
    private Long id;
    private final SourceType sourceType;
    private final Long externalId;
    private final String name;
    private final Integer price;
    private final Integer stock;

    private WishlistItem(Long id, SourceType sourceType, Long externalId, String name, Integer price, Integer stock) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("이름이 비어 있습니다.");
        }

        if (price == null || price < 0) {
            throw new IllegalArgumentException("가격은 0 이상이어야 합니다.");
        }

        if (sourceType == SourceType.MANUAL ^ externalId == null) {
            throw new IllegalArgumentException("sourceType과 externalId 조합이 맞지 않습니다.");
        }

        this.sourceType = sourceType;
        this.externalId = externalId;
        this.id = id;
        this.name = name.strip();
        this.price = price;
        this.stock = stock;
    }

    public static WishlistItem fromManual(Long id, String name, Integer price) {
        return new WishlistItem(id, SourceType.MANUAL, null, name, price, null);
    }

    public static WishlistItem fromExternal(Long id, Long externalId, String name, Integer price, Integer stock) {
        return new WishlistItem(id, SourceType.EXTERNAL, externalId, name, price, stock);
    }

    public static WishlistItem fromExternal(ExternalProduct externalProduct) {
        return WishlistItem.fromExternal(null, externalProduct.id(), externalProduct.title(), externalProduct.getConvertedPrice(), externalProduct.stock());
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public SourceType getSourceType() {
        return sourceType;
    }

    public Long getExternalId() {
        return externalId;
    }

    public String getName() {
        return name;
    }

    public Integer getPrice() {
        return price;
    }

    public Integer getStock() {
        return stock;
    }

    public boolean hasSameId(Long id) {
        return this.id.equals(id);
    }

    public boolean isWithinBudget(Integer budget) {
        return price <= budget;
    }

    public boolean isExternal(Long externalId) {
        return sourceType == SourceType.EXTERNAL && this.externalId.equals(externalId);
    }
}
