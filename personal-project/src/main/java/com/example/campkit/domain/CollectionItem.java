
package com.example.campkit.domain;

public class CollectionItem {

    private final Long id;
    private final String name;
    private final long price;
    private final SourceType sourceType;
    private final Long externalId;
    private final Integer stock;

    // 기존 수동 등록
    public CollectionItem(Long id, String name, long price) {
        this(id, name, price, SourceType.MANUAL, null, null);
    }

    // 외부 상품 등록
    public CollectionItem(Long id, String name, long price, SourceType sourceType, Long externalId, Integer stock) {

        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("장비 이름은 비어 있을 수 없습니다.");
        }

        if (price < 0) {
            throw new IllegalArgumentException("가격은 0원 이상이어야 합니다.");
        }

        if (sourceType == null) {
            throw new IllegalArgumentException("상품 출처가 필요합니다.");
        }

        if (sourceType == SourceType.DUMMYJSON && (externalId == null || externalId <= 0 || stock == null || stock < 0)) {
            throw new IllegalArgumentException("외부 상품 정보가 올바르지 않습니다.");
        }

        this.id = id;
        this.name = name.trim();
        this.price = price;
        this.sourceType = sourceType;
        this.externalId = externalId;
        this.stock = stock;
    }

    public CollectionItem withId(long newId) {
        return new CollectionItem(
                newId, name, price, sourceType, externalId, stock
        );
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public long getPrice() { return price; }
    public SourceType getSourceType() { return sourceType; }
    public Long getExternalId() { return externalId; }
    public Integer getStock() { return stock; }
}
