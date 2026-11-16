package com.example.personal_project.domain;

public class Item {

    private Long id;
    private String name;
    private int price;

    private SourceType sourceType;
    private Long externalId;
    private Integer stock;

    public Item(Long id,String name,int price) {
        this(id, name, price, SourceType.MANUAL, null, null);
    }

    public Item(Long id,String name,int price,
            SourceType sourceType,
            Long externalId,
            Integer stock) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.sourceType = sourceType;
        this.externalId = externalId;
        this.stock = stock;
    }

    public Long getId() {
        return id;
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
