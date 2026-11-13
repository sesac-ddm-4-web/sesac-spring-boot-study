package com.example.wishlist.management.domain.wish;

import java.util.Objects;

// domain/Wish.java
public class Wish {
    private static final int MAX_NAME_LENGTH = 100;
    private static final int MAX_MANUAL_PRICE = 1_000_000;

    private Long id;
    private final String name;
    private final Integer price;
    private final SourceType sourceType;
    private final Long externalId;   // 직접 등록이면 null
    private final Integer stock;     // 직접 등록이면 null (재고 제한 없음)

    // 직접 등록용. 기존 코드와 테스트가 쓰던 생성자를 그대로 유지
    public Wish(Long id, String name, Integer price) {
        this(id, name, price, SourceType.MANUAL, null, null);
    }

    // 외부 상품 가져오기용 (정적 팩토리 메서드)
    // externalId는 externalId에 지정된다.
    // 0L: 임시값
    public static Wish external(Long externalId, String name, Integer price, Integer stock) {
        return new Wish(0L, name, price, SourceType.EXTERNAL, externalId, stock);
    }

    public Wish(Long id, String name, Integer price,
                SourceType sourceType, Long externalId, Integer stock) {
        if (name == null || price == null) {
            throw new IllegalArgumentException("이름과 가격은 필수입니다.");
        }
        String trimmed = name.trim();
        if (trimmed.isEmpty() || trimmed.length() > MAX_NAME_LENGTH) {
            throw new IllegalArgumentException("이름은 1~100자여야 합니다.");
        }
        if (price < 0) {
            throw new IllegalArgumentException("가격은 0 이상이어야 합니다. price=" + price);
        }
        if (sourceType == SourceType.MANUAL && price > MAX_MANUAL_PRICE) {
            throw new IllegalArgumentException("가격의 범위는 0~100만원입니다. price=" + price);
        }
        this.id = id;
        this.name = trimmed;
        this.price = price;
        this.sourceType = sourceType;
        this.externalId = externalId;
        this.stock = stock;
    }

    public void setId(Long id) { this.id = id; }
    public Long getId() { return id; }
    public String getName() { return name; }
    public Integer getPrice() { return price; }
    public SourceType getSourceType() { return sourceType; }
    public Long getExternalId() { return externalId; }
    public Integer getStock() { return stock; }

    public boolean sameId(Long id) {
        return Objects.equals(this.id, id);
    }    public boolean isExternal() { return sourceType == SourceType.EXTERNAL; }
    public boolean sameExternalId(Long externalId) {
        return this.externalId != null && this.externalId.equals(externalId);
    }
}