package com.example.wishlist.cart.domain;

import com.example.wishlist.cart.exception.CartItemNotFoundException;
import com.example.wishlist.cart.exception.CartRuleViolationException;
import com.example.wishlist.item.domain.WishlistItem;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class Cart {

    public static final int MAX_KINDS = 20;

    private final List<CartItem> items;
    private long lastItemId;

    public Cart() {
        this(new ArrayList<>(), 0L);
    }

    private Cart(List<CartItem> items, long lastItemId) {
        this.items = items;
        this.lastItemId = lastItemId;
    }

    // 복사본 만들기 (repository 저장/조회 때 사용)
    public Cart copy() {
        return new Cart(new ArrayList<>(items), lastItemId);
    }

    // 담기 (같은 상품이면 수량 합산, 처음이면 새로 복사해서 추가)
    public CartItem add(WishlistItem product, int quantity) {
        CartItem.validateQuantity(quantity);

        Optional<CartItem> sameProduct = findByWishlistItemId(product.getId());
        if (sameProduct.isPresent()) {
            // 처음 복사한 단가·재고를 계속 사용
            CartItem merged = sameProduct.get().addQuantity(quantity);
            replace(merged);
            return merged;
        }

        if (product.isSoldOut()) {
            throw new CartRuleViolationException("품절된 상품은 장바구니에 담을 수 없습니다. (" + product.getName() + ")");
        }
        if (items.size() >= MAX_KINDS) {
            throw new CartRuleViolationException("장바구니에는 최대 " + MAX_KINDS + "종까지 담을 수 있습니다.");
        }

        CartItem created = CartItem.snapshotOf(lastItemId + 1, product, quantity);
        lastItemId++;   // 검증을 모두 통과한 뒤에만 id를 사용
        items.add(created);
        return created;
    }

    // 수량 변경
    public CartItem changeQuantity(Long cartItemId, int quantity) {
        CartItem changed = getItem(cartItemId).changeQuantity(quantity);
        replace(changed);
        return changed;
    }

    // 수량 변경
    public void remove(Long cartItemId) {
        items.remove(getItem(cartItemId));
    }

    // 상품 합계
    public long subtotal() {
        long sum = 0L;
        for (CartItem item: items) {
            sum = Math.addExact(sum, item.lineTotal());
        }
        return sum;
    }

    public int totalQuantity() {
        return items.stream().mapToInt(CartItem::getQuantity).sum();
    }

    public boolean isEmpty() {
        return items.isEmpty();
    }

    // 담은 순서대로, 수정할 수 없는 목록
    public List<CartItem> getItems() {
        return List.copyOf(items);
    }

    public CartItem getItem(Long cartItemId) {
        return items.stream()
            .filter(item -> item.getId().equals(cartItemId))
            .findFirst()
            .orElseThrow(() -> new CartItemNotFoundException(cartItemId));
    }

    private Optional<CartItem> findByWishlistItemId(Long wishlistItemId) {
        return items.stream()
            .filter(item -> item.getWishlistItemId().equals(wishlistItemId))
            .findFirst();
    }

    private void replace(CartItem updated) {
        for (int i = 0; i < items.size(); i++) {
            if (items.get(i).getId().equals(updated.getId())) {
                items.set(i, updated);
                return;
            }
        }
    }

    // 지금까지 사용한 마지막 행 id (저장소가 저장할 때 사용)
    public long getLastItemId() {
        return lastItemId;
    }

    // DB -> 도메인
    public static Cart restore(List<CartItem> items, long lastItemId) {
        long maxId = items.stream().mapToLong(CartItem::getId).max().orElse(0L);
        return new Cart(new ArrayList<>(items), Math.max(lastItemId, maxId));
    }
}
