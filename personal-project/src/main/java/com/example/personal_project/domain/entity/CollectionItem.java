package com.example.personal_project.domain.entity;

import com.example.personal_project.presentation.dto.collectionItem.CollectionItemRequest;
import com.example.personal_project.presentation.dto.dummyJsonProduct.DummyJsonProduct;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class CollectionItem {
    private Long id;
    @Size(min = 1,max = 40,message = "상품 이름은 1~40글자입니다.")
    private String name;

    @Min(value = 0,message = "가격은 0이상입니다.")
    private Integer price;

    private ItemType itemType;

    private Long externalId;
    private Integer stock;
    public static final int EXCHANGE_RATE = 1_000;
    public static final int RECOMMEND_NUM = 3;

    public CollectionItem() {
    }

    public CollectionItem(String name, Integer price, ItemType itemType) {
        this.name = name;
        this.price = price;
        this.itemType=itemType;
    }

    public CollectionItem(String name, Integer price, ItemType itemType, Long externalId,Integer stock) {
        this.name = name;
        this.price = price;
        this.itemType=itemType;
        this.externalId=externalId;
        this.stock=stock;
    }

    public Boolean sameId(Long id){
        return this.id.equals(id);
    }

    public static CollectionItem createInternalItem(CollectionItemRequest dto){
        return new CollectionItem(dto.name().trim(), dto.price(), ItemType.INTERNAL);
    }

    public static CollectionItem createExternalItem(DummyJsonProduct dto){
        BigDecimal result = dto.price()
                .multiply(BigDecimal.valueOf(EXCHANGE_RATE))
                .setScale(0, RoundingMode.HALF_UP);
        return new CollectionItem(dto.title(), result.intValue(), ItemType.EXTERNAL, dto.id(), dto.stock());
    }

    public Boolean isInternalItem(){
        return itemType.equals(ItemType.INTERNAL);
    }

    public Long getExternalId() {
        return externalId;
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

    public Integer getStock() {
        return stock;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPrice(Integer price) {
        this.price = price;
    }

    public void setItemType(ItemType itemType) {
        this.itemType = itemType;
    }

    public void setExternalId(Long externalId) {
        this.externalId = externalId;
    }

    public void setStock(Integer stock) {
        this.stock = stock;
    }

    public String getItemType() {
        return itemType.name();
    }
    public ItemType getEnumItemType(){
        return itemType;
    }
}
