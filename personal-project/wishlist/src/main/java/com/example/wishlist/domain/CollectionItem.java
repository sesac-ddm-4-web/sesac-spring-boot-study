package com.example.wishlist.domain;

public class CollectionItem {
    private Long id;
    private String name;
    private int price;

    public CollectionItem(String name, int price) {
        this.name = name;
        this.price = price;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public int getPrice() {
        return price;
    }
}
