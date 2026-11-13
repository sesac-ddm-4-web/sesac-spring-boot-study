package com.example.wishlist.item.domain;

import com.example.wishlist.item.exception.ImportedItemNotEditableException;
import com.example.wishlist.item.exception.InvalidItemException;

public class WishlistItem {

    private final Long id;
    private final String name;
    private final int price;
    private final SourceType sourceType;
    private final Long externalId;
    private final Integer stock;

    // 직접 등록한 item
    public WishlistItem (Long id, String name, int price) {
        this(id, name, price, SourceType.MANUAL, null, null);
    }

    private WishlistItem(Long id, String name, int price,
        SourceType sourceType, Long externalId, Integer stock) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.sourceType = sourceType;
        this.externalId = externalId;
        this.stock = stock;
    }

    // 직접 등록
    public static WishlistItem manual(String name, Integer price) {
        validateName(name);
        validatePrice(price);
        return new WishlistItem(null, name.strip(), price);
    }

    // 외부 상품 가져오기
    public static WishlistItem imported(long externalId, String name, int price, int stock) {
        return new WishlistItem(null, name, price, SourceType.EXTERNAL, externalId, stock);
    }

    // id 부여할때 같은 내용에 id만 붙여서 새로운 객체 생성
    public WishlistItem withId(Long id) {
        return new WishlistItem(id, name, price, sourceType, externalId, stock);
    }

    // 직접 등록한 상품 이름/가격 수정
    public WishlistItem changeTo(String name, Integer price) {
        if (isImported()) {
            throw new ImportedItemNotEditableException(id);
        }
        return manual(name, price).withId(id);
    }

    public boolean isImported() {
        return sourceType == SourceType.EXTERNAL;
    }

    // 재고 제한이 있는지 체크
    public boolean hasStockLimit() {
        return stock != null;
    }

    // 품절
    public boolean isSoldOut() {
        return hasStockLimit() && stock == 0;
    }

    private static void validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new InvalidItemException("이름은 비어있을 수 없습니다.");
        }
    }

    private static void validatePrice(Integer price) {
        if (price == null || price < 0) {
            throw new InvalidItemException("가격은 0 이상의 정수여야 합니다.");
        }
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

    @Override
    public String toString() {
        return "WishlistItem{id=" + id + ", name='" + name + "', price=" + price
            + ", sourceType=" + sourceType + ", externalId=" + externalId + ", stock=" + stock + "}";
    }

    // DB -> domain
    public static WishlistItem restore(Long id, String name, int price, SourceType sourceType, Long externalId, Integer stock) {
        return new WishlistItem(id, name, price, sourceType, externalId, stock);
    }
}
