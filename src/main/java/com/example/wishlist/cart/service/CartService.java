package com.example.wishlist.cart.service;

import com.example.wishlist.cart.domain.Cart;
import com.example.wishlist.cart.domain.CartItem;
import com.example.wishlist.cart.repository.CartRepository;
import com.example.wishlist.cart.repository.DatabaseCartRepository;
import com.example.wishlist.coupon.domain.Coupon;
import com.example.wishlist.coupon.domain.CouponComparison;
import com.example.wishlist.coupon.domain.PriceBreakdown;
import com.example.wishlist.item.domain.WishlistItem;
import com.example.wishlist.item.exception.ItemNotFoundException;
import com.example.wishlist.item.repository.DatabaseWishlistItemRepository;
import com.example.wishlist.item.repository.WishlistItemRepository;
import org.springframework.stereotype.Service;

@Service
public class CartService {

    private final DatabaseCartRepository cartRepository;
    private final DatabaseWishlistItemRepository wishlistItemRepository;

    public CartService(DatabaseCartRepository cartRepository, DatabaseWishlistItemRepository wishlistItemRepository) {
        this.cartRepository = cartRepository;
        this.wishlistItemRepository = wishlistItemRepository;
    }

    public Cart getCart() {
        return cartRepository.load();
    }

    // 장바구니에 담기
    public synchronized CartAddResult add(Long wishlistItemId, int quantity) {
        WishlistItem product = wishlistItemRepository.findById(wishlistItemId)
            .orElseThrow(() -> new ItemNotFoundException(wishlistItemId));

        Cart cart = cartRepository.load();
        int kindsBefore = cart.getItems().size();
        CartItem added = cart.add(product, quantity);
        cartRepository.save(cart);

        return new CartAddResult(added, cart.getItems().size() > kindsBefore);
    }

    // 수량 변경
    public synchronized CartItem changeQuantity(Long cartItemId, int quantity) {
        Cart cart = cartRepository.load();
        CartItem changed = cart.changeQuantity(cartItemId, quantity);
        cartRepository.save(cart);
        return changed;
    }

    // 삭제
    public synchronized void remove(Long cartItemId) {
        Cart cart = cartRepository.load();
        cart.remove(cartItemId);
        cartRepository.save(cart);
    }

    // 금액 미리보기
    public PriceBreakdown preview(String couponCode) {
        Coupon coupon = Coupon.from(couponCode);
        return PriceBreakdown.calculate(cartRepository.load().subtotal(), coupon);
    }

    // 최종 금액이 가장 작은 쿠폰 찾기
    public CouponComparison findBestCoupon() {
        return CouponComparison.of(cartRepository.load().subtotal());
    }
}
