package com.example.wishlist.cart.service;

import com.example.wishlist.cart.domain.CartItem;

// 장바구니 담기 결과
public record CartAddResult(CartItem item, boolean newLine) {
}
