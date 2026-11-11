package com.example.demo.item.domain;

import com.example.demo.global.exception.ConflictException;
import jakarta.validation.constraints.*;

import java.util.Objects;

public class CollectionItem {
    private Long id;

    @NotBlank
    @Size(max = 100)
    private String name;

    @NotNull
    @Max(100000000)
    @Min(0)
    private Integer price;

    @NotNull
    private SourceType sourceType;

    private Long externalId;

    @Min(0)
    private Integer stock; // 외부 재고, 수동이면 null

    public CollectionItem(Long id, String name, Integer price) {
        this(id, name, price, SourceType.MANUAL, null, null);
    }

    private CollectionItem(Long id, String name, Integer price,
                           SourceType sourceType, Long externalId, Integer stock) {
        this.id = id;
        this.name = name == null ? null : name.strip();
        this.price = price;
        this.sourceType = sourceType;
        this.externalId = externalId;
        this.stock = stock;
    }

    public static CollectionItem ofExternal(Long externalId, String name, Integer price, Integer stock) {
        return new CollectionItem(null, name, price, SourceType.EXTERNAL, externalId, stock);
    }

    public boolean isExternal() {
        return sourceType == SourceType.EXTERNAL;
    }

    public void checkEditable() {
        if (isExternal()) {
            throw new ConflictException("외부에서 가져온 상품은 수정할 수 없습니다.");
        }
    }

    public Boolean sameId(Long id){
        return this.id.equals(id);
    }

    @Override
    public boolean equals(Object o){
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        CollectionItem collectionItem = (CollectionItem) o;
        return Objects.equals(id, collectionItem.id);
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

    public SourceType getSourceType() { return sourceType; }

    public Long getExternalId() { return externalId; }

    public Integer getStock() { return stock; }
}
