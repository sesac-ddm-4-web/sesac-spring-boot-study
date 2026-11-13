package com.example.sesac_spring_boot_study.domain;

public class Product {
    private Long id;
    private String name;
    private Integer price;
    private SourceType sourceType;
    private Long externalId;
    private Integer stock;

    public Product(Long id, String name, int price) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.sourceType = SourceType.MANUAL;
    }

    public Product () {}

    public void setSourceType(SourceType sourceType) {
        this.sourceType = sourceType;
    }

    public void setExternalId(Long externalId) {
        this.externalId = externalId;
    }

    public void setStock(Integer stock) {
        this.stock = stock;
    }

    public Product(Long id, String name, Integer price, SourceType sourceType, Long externalId, Integer stock){
        this.id = id;
        this.name = name;
        this.price = price;
        this.sourceType = sourceType;
        this.externalId = externalId;
        this.stock = stock;
    }

    public SourceType getSourceType() {return sourceType;}

    public Long getExternalId() {return externalId;}

    public Integer getStock() {return stock;}

    public void setId(Long id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPrice(Integer price) {
        this.price = price;
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

    public Boolean sameId(Long id) {
        return this.id.equals(id);
    }


}
