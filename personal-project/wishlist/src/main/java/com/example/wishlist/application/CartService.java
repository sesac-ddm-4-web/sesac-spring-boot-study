package com.example.wishlist.application;

import com.example.wishlist.domain.Cart;
import com.example.wishlist.domain.CartItem;
import com.example.wishlist.domain.CartItemNotFoundException;
import com.example.wishlist.domain.CartItemRepository;
import com.example.wishlist.domain.CollectionItem;
import com.example.wishlist.domain.CollectionItemRepository;
import com.example.wishlist.domain.Coupon;
import com.example.wishlist.domain.ItemNotFoundException;
import com.example.wishlist.domain.PaymentPreview;
import org.springframework.stereotype.Service;

@Service
public class CartService {
    private final CartItemRepository cartRepository;
    private final CollectionItemRepository wishRepository;

    public CartService(CartItemRepository cartRepository, CollectionItemRepository wishRepository) {
        this.cartRepository = cartRepository;
        this.wishRepository = wishRepository;
    }

    public CartItem add(Long wishItemId, int quantity) {
        CollectionItem wishItem = wishRepository.findById(wishItemId);
        if (wishItem == null) {
            throw new ItemNotFoundException(wishItemId);
        }

        CartItem existing = cartRepository.findByWishItemId(wishItemId);
        if (existing != null) {
            existing.addQuantity(quantity);
            return cartRepository.update(existing);
        }

        new Cart(cartRepository.findAll()).checkCanAddNewKind();
        CartItem cartItem = CartItem.copyOf(wishItem, quantity);
        return cartRepository.add(cartItem);
    }

    public Cart findCart() {
        return new Cart(cartRepository.findAll());
    }

    public CartItem changeQuantity(Long cartItemId, int quantity) {
        CartItem cartItem = findCartItem(cartItemId);
        cartItem.changeQuantity(quantity);
        return cartRepository.update(cartItem);
    }

    public void remove(Long cartItemId) {
        if (!cartRepository.delete(cartItemId)) {
            throw new CartItemNotFoundException(cartItemId);
        }
    }

    public PaymentPreview preview(String couponCode) {
        Coupon coupon = Coupon.from(couponCode);
        return findCart().preview(coupon);
    }

    private CartItem findCartItem(Long cartItemId) {
        CartItem cartItem = cartRepository.findById(cartItemId);
        if (cartItem == null) {
            throw new CartItemNotFoundException(cartItemId);
        }
        return cartItem;
    }
}
