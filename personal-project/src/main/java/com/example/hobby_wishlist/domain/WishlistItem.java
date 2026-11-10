package com.example.hobby_wishlist.domain;

public class WishlistItem {
    private Long id;

    private final SourceType sourceType;
    private final Long externalId;

    private final String name;

    private final Integer price;

    private WishlistItem(Long id, SourceType sourceType, Long externalId, String name, Integer price) {
        this.sourceType = sourceType;
        this.externalId = externalId;
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("이름이 비어 있습니다.");
        }

        if (price == null || price < 0) {
            throw new IllegalArgumentException("가격은 0 이상이어야 합니다.");
        }

        if (sourceType == SourceType.MANUAL ^ externalId == null) {
            throw new IllegalArgumentException("sourceType과 externalId 조합이 맞지 않습니다.");
        }

        this.id = id;
        this.name = name.strip();
        this.price = price;
    }

    public static WishlistItem fromManual(Long id, String name, Integer price) {
        return new WishlistItem(id, SourceType.MANUAL, null, name, price);
    }

    public static WishlistItem fromExternal(Long id, Long externalId, String name, Integer price) {
        return new WishlistItem(id, SourceType.EXTERNAL, externalId, name, price);
    }

    public static WishlistItem fromExternal(ExternalProduct externalProduct) {
        return WishlistItem.fromExternal(null, externalProduct.id(), externalProduct.title(), externalProduct.getConvertedPrice());
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Integer getPrice() {
        return price;
    }

    public boolean sameId(Long id) {
        return this.id.equals(id);
    }

    public boolean isWithinBudget(Integer budget) {
        return price <= budget;
    }
}
