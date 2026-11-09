package com.example.hobby_wishlist.domain;

public class WishlistItem {
    private Long id;

    private String name;

    private Integer price;

    private WishlistItem(Long id, String name, Integer price) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("이름이 비어 있습니다.");
        }

        if (price == null || price < 0) {
            throw new IllegalArgumentException("가격은 0 이상이어야 합니다.");
        }

        this.id = id;
        this.name = name.strip();
        this.price = price;
    }

    public static WishlistItem create(Long id, String name, Integer price) {
        return new WishlistItem(id, name, price);
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

    public void setId(Long id) {
        this.id = id;
    }

    public boolean sameId(Long id) {
        return this.id.equals(id);
    }

    public boolean isWithinBudget(Integer budget) {
        return price <= budget;
    }
}
