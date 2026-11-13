package com.example.hobby_wishlist.domain.cart;

public interface CartRepository {
    Cart getCart();
    Cart saveChanges(Cart cart);
}
