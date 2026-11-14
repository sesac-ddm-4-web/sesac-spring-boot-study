package com.example.wishlist.domain;

public class CollectionItem {
    private Long id;
    private String name;
    private int price;
    private SourceType sourceType;
    private Long externalId;
    private Integer stock;

    public CollectionItem(String name, int price) {
        this(name, price, SourceType.MANUAL, null, null);
    }

    public CollectionItem(String name, int price, SourceType sourceType, Long externalId, Integer stock) {
        this.name = name;
        this.price = price;
        this.sourceType = sourceType;
        this.externalId = externalId;
        this.stock = stock;
    }

    public boolean isExternal() {
        return sourceType != SourceType.MANUAL;
    }

    public void update(String name, int price) {
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

    public SourceType getSourceType() {
        return sourceType;
    }

    public Long getExternalId() {
        return externalId;
    }

    public Integer getStock() {
        return stock;
    }
}
