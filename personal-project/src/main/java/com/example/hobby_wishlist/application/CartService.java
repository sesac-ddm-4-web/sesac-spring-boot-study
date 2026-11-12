package com.example.hobby_wishlist.application;

import com.example.hobby_wishlist.domain.cart.Cart;
import com.example.hobby_wishlist.domain.cart.CartRepository;
import com.example.hobby_wishlist.domain.wishlist.WishlistItem;
import com.example.hobby_wishlist.domain.wishlist.WishlistRepository;
import com.example.hobby_wishlist.dto.cart.AddCartItemRequest;
import com.example.hobby_wishlist.dto.cart.CartResponse;
import com.example.hobby_wishlist.dto.cart.ChangeCartItemQuantityRequest;
import org.springframework.stereotype.Service;

@Service
public class CartService {
    private final CartRepository cartRepository;
    private final WishlistRepository wishlistRepository;

    public CartService(CartRepository cartRepository, WishlistRepository wishlistRepository) {
        this.cartRepository = cartRepository;
        this.wishlistRepository = wishlistRepository;
    }

    public synchronized CartResponse addItem(AddCartItemRequest request) {
        // 장바구니 가져오기
        WishlistItem wishlistItem = wishlistRepository.findById(request.wishlistId());
        Cart cart = cartRepository.getCart();

        // 장바구니에 상품 추가하고 장바구니 갱신하기
        cart.add(wishlistItem, request.quantity());

        Cart updatedCart = cartRepository.saveChanges(cart);
        return CartResponse.from(updatedCart);
    }

    public synchronized CartResponse getAllItems() {
        return CartResponse.from(cartRepository.getCart());
    }

    public synchronized CartResponse changeQuantity(Long cartItemId, ChangeCartItemQuantityRequest request) {
        // 장바구니에 있는 상품 수량 변경하고 장바구니 갱신하기
        Cart cart = cartRepository.getCart();
        cart.changeQuantity(cartItemId , request.quantity());

        Cart updatedCart = cartRepository.saveChanges(cart);
        return CartResponse.from(updatedCart);
    }

    public synchronized CartResponse removeItem(Long cartItemId) {
        // 장바구니에서 항목 삭제하고 장바구니 갱신하기
        Cart cart = cartRepository.getCart();
        cart.remove(cartItemId);

        Cart updatedCart = cartRepository.saveChanges(cart);
        return CartResponse.from(updatedCart);
    }
}
