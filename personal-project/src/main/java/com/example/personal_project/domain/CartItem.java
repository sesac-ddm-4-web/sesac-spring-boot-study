package com.example.personal_project.domain;

// 상품 한 종류의 수량·재고 검사
public class CartItem {
    private Long id;
    private Long itemId;
    private String name;
    private long unitPrice;
    private  SourceType sourceType;
    private Integer stock;
    private int quantity;


    public CartItem(Long id, Long itemId, String name, long unitPrice, SourceType sourceType, Integer stock, int quantity) {
        // 수량·재고 검사
        validateQuantity(quantity, sourceType, stock);

        this.id = id;
        this.itemId = itemId;
        this.name = name;
        this.unitPrice = unitPrice;
        this.sourceType = sourceType;
        this.stock = stock;
        this.quantity = quantity;
    }

    public Long getId() {
        return id;
    }

    public Long getItemId() {
        return itemId;
    }

    public String getName() {
        return name;
    }

    public long getUnitPrice() {
        return unitPrice;
    }

    public SourceType getSourceType() {
        return sourceType;
    }

    public Integer getStock() {
        return stock;
    }

    public int getQuantity() {
        return quantity;
    }

    public void changeQuantity(int quantity) {
        // 수량·재고 검사
        validateQuantity(quantity, sourceType, stock);
        // 수량만 변경
        this.quantity = quantity;
    }

    private void validateQuantity(
            int quantity, SourceType sourceType, Integer stock) {

        // 수량·재고 검사
        if (quantity <1 || quantity > 5) {
            throw new IllegalArgumentException(
                    "수량은 1개 이상 5개 이하여야 합니다."
            );
        }

        // 외부 상품은
        if (sourceType == SourceType.DUMMY_JSON) {
            // 재고 정보를 받지 못함 → 거부
            if (stock == null) {
                throw new IllegalStateException("외부 상품의 재고를 확인할 수 없습니다.");
            }
            // 재고 이하만 허용
            // 재고가 0(품절)일 때, 재고보다 담으려는 수량이 많을 때
            if (quantity > stock) {
                throw new IllegalStateException("재고보다 많은 수량을 담을 수 없습니다.");
            }
        }
    }
}
