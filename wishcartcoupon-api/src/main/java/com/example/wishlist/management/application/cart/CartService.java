package com.example.wishlist.management.application.cart;

import com.example.wishlist.management.domain.cart.*;
import com.example.wishlist.management.domain.wish.Wish;
import com.example.wishlist.management.domain.wish.WishRepository;
import org.springframework.stereotype.Service;

import java.util.List;

// get() 메서드를 통해 Cart 접근
@Service
public class CartService {
    private final CartRepository cartRepository;
    private final WishRepository wishRepository;

    public CartService(CartRepository cartRepository, WishRepository wishRepository) {
        this.cartRepository = cartRepository;
        this.wishRepository = wishRepository;
    }

    public CartItem addItem(long wishId, int quantity) {
        Wish wish = wishRepository.getWish(wishId);          // 없으면 404
        Cart cart = cartRepository.get();
        CartItem item = cart.add(wish, quantity);
        cartRepository.save(cart);
        return item;
    }

    public List<CartItem> getItems() {
        return cartRepository.get().getItems();
    }

    // 장바구니 행 id만 사용하므로 원본 위시가 삭제돼도 동작한다
    public CartItem changeQuantity(long itemId, int quantity) {
        Cart cart = cartRepository.get();
        CartItem item = cart.changeQuantity(itemId, quantity);
        cartRepository.save(cart);
        return item;
    }

    public void removeItem(long itemId) {
        Cart cart = cartRepository.get();
        cart.remove(itemId);
        cartRepository.save(cart);
    }

    public CartPreview preview(String coupon) {
        return cartRepository.get().preview(CouponType.from(coupon));
    }

    public CouponRecommendation recommendCoupon(){
        return cartRepository.get().recommendCoupon();
    }
}