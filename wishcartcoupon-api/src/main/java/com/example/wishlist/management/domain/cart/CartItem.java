package com.example.wishlist.management.domain.cart;

import com.example.wishlist.management.domain.wish.Wish;
import com.example.wishlist.management.domain.exception.ConflictException;

public class CartItem {
    public static final int MIN_QUANTITY = 1;
    public static final int MAX_QUANTITY = 5;

    private final long id;
    private final long wishId;
    private final String name;
    private final int price;
    private final Integer stock;     // null이면 재고 제한 없음
    private int quantity;

    private CartItem(long id, long wishId, String name, int price, Integer stock, int quantity) {
        this.id = id;
        this.wishId = wishId;
        this.name = name;
        this.price = price;
        this.stock = stock;
        this.quantity = quantity;
    }

    // 처음 담을 때: 위시의 현재 값을 복사해서 새 행을 만든다
    // 장바구니 담기 전, 품절 상품인지 체크
    static CartItem copyOf(long id, Wish wish, int quantity) {
        requiredValidQuantity(quantity);
        Integer stock = wish.getStock();
        if (stock != null && stock == 0) {
            throw new ConflictException("품절된 상품은 담을 수 없습니다. wishId=" + wish.getId());
        }
        requiredWithinStock(stock, quantity);
        return new CartItem(id, wish.getId(), wish.getName(), wish.getPrice(), stock, quantity);  // 생성자 순서와 같게
    }

    // 수량을 newQuantity로 바꾼다. 검사를 모두 통과한 뒤에만 값을 바꾼다
    void changeQuantity(int newQuantity) {
        requiredValidQuantity(newQuantity);
        requiredWithinStock(stock, newQuantity);      // 재고와 새 수량을 비교
        this.quantity = newQuantity;
    }

    // 이미 담긴 상품에 amount만큼 더한다
    void increaseBy(int amount) {
        requiredValidQuantity(amount);
        int sum = quantity + amount;
        if (sum > MAX_QUANTITY) {
            throw new ConflictException("한 상품의 수량 합계는 " + MAX_QUANTITY
                    + " 이하여야 합니다. 현재=" + quantity + ", 추가=" + amount);
        }
        requiredWithinStock(stock, sum);              // 재고와 합산 결과를 비교
        this.quantity = sum;
    }

    public long lineAmount() {
        return (long) price * quantity;
    }

    boolean sameId(long id) { return this.id == id; }
    boolean sameWish(long wishId) { return this.wishId == wishId; }

    // 수량 검사
    private static void requiredValidQuantity(int quantity) {
        if (quantity < MIN_QUANTITY || quantity > MAX_QUANTITY) {
            throw new IllegalArgumentException("수량은 1~5여야 합니다. quantity=" + quantity);
        }
    }

    // 재고 초과 검사
    private static void requiredWithinStock(Integer stock, int quantity) {
        if (stock != null && quantity > stock) {
            throw new ConflictException("재고를 초과할 수 없습니다. stock=" + stock + ", 요청 수량=" + quantity);
        }
    }

    // CartItem.java에 추가 (infrastructure가 다른 패키지라서 public)
    public static CartItem restore(long id, long wishId, String name,
                                   int price, Integer stock, int quantity) {
        return new CartItem(id, wishId, name, price, stock, quantity);
    }

    public long getId() { return id; }
    public long getWishId() { return wishId; }
    public String getName() { return name; }
    public int getPrice() { return price; }
    public Integer getStock() { return stock; }
    public int getQuantity() { return quantity; }
}