package com.example.wishlist.management.domain.cart;

import com.example.wishlist.management.domain.exception.ConflictException;
import com.example.wishlist.management.domain.exception.NotFoundException;
import com.example.wishlist.management.domain.wish.Wish;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class Cart {
    // 상품 종류
    public static final int MAX_KINDS = 20;
    // 무료 배송
    private static final long FREE_SHIPPING_THRESHOLD = 100000;
    // 일반 배근
    private static final long SHIPPING_FEE = 5_000;

    private final List<CartItem> items = new ArrayList<>();
    private long nextItemId = 1L;

    // 담기: 이미 있으면 수량 합산, 없으면 복사해서 새 행 추가
    public CartItem add(Wish wish, Integer quantity){
        Optional<CartItem> existing = items.stream()
                .filter(item -> item.sameWish(wish.getId()))
                .findFirst();
        if(existing.isPresent()){
            existing.get().increaseBy(quantity);
            return existing.get();
        }
        if(items.size() >= MAX_KINDS){
            throw new ConflictException("장바구니에는 최대 " + MAX_KINDS + "종류까지 담을 수 있습니다.");
        }
        CartItem item = CartItem.copyOf(nextItemId,wish,quantity);
        items.add(item);
        nextItemId++;
        return item;
    }

    public CartItem changeQuantity(long itemId, Integer newquantity){
        CartItem target = findItem(itemId);
        target.changeQuantity(newquantity);
        return target;
    }

    public void remove(long itemId){
        items.remove(findItem(itemId));
    }

    public List<CartItem> getItems() {
        return List.copyOf(items);
    }

    public long subtotal(){
        return items.stream().mapToLong(CartItem::lineAmount).sum();
    }

    // Cart.java : 기존 preview 메서드를 아래 4개로 교체
    public CartPreview preview(CouponType coupon) {
        if (!canUse(coupon)) {
            throw new IllegalArgumentException(
                    "사용할 수 없는 쿠폰입니다. coupon=" + coupon + ", subtotal=" + subtotal());
        }
        return calculate(coupon);
    }

    // 쓸 수 있는 쿠폰을 NONE → PERCENT10 → FIXED15000 순서로 계산해 최종 금액이 가장 낮은 것을 고른다
    public CouponRecommendation recommendCoupon() {
        List<CartPreview> candidates = new ArrayList<>();
        CartPreview best = null;
        for (CouponType coupon : CouponType.values()) {
            if (!canUse(coupon)) continue;
            CartPreview candidate = calculate(coupon);
            candidates.add(candidate);
            if (best == null || candidate.finalAmount() < best.finalAmount()) {  // 같으면 바꾸지 않는다 → 앞선 쿠폰이 이김
                best = candidate;
            }
        }
        return new CouponRecommendation(best.coupon(), candidates);
    }

    private boolean canUse(CouponType coupon) {
        if (items.isEmpty()) return coupon == CouponType.NONE;   // 빈 장바구니는 NONE만
        return coupon.isUsable(subtotal());
    }

    // 계산만 한다. 장바구니를 바꾸지 않는다.
    private CartPreview calculate(CouponType coupon) {
        if (items.isEmpty()) return new CartPreview(0, 0, 0, 0, 0, CouponType.NONE);
        long subtotal = subtotal();
        long discount = coupon.discount(subtotal);
        long discountedSubtotal = subtotal - discount;
        long shipping = discountedSubtotal >= FREE_SHIPPING_THRESHOLD ? 0 : SHIPPING_FEE;
        return new CartPreview(subtotal, discount, discountedSubtotal,
                shipping, discountedSubtotal + shipping, coupon);
    }

    private CartItem findItem(long itemId){
        return items.stream()
                .filter(item -> item.sameId(itemId))
                .findFirst()
                .orElseThrow(() -> new NotFoundException("장바구니 항목을 찾을 수 없습니다. itemId=" + itemId));
    }

    // Cart.java에 추가
    public static Cart restore(List<CartItem> savedItems) {
        Cart cart = new Cart();
        cart.items.addAll(savedItems);
        long maxId = savedItems.stream().mapToLong(CartItem::getId).max().orElse(0L);
        cart.nextItemId = maxId + 1;     // 다음에 담을 항목의 id가 겹치지 않게
        return cart;
    }
}
