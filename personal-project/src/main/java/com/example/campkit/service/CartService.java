
package com.example.campkit.service;

import com.example.campkit.domain.*;
import com.example.campkit.dto.CartPreview;
import com.example.campkit.exception.NotFoundException;
import com.example.campkit.repository.CartRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CartService {

    private final CartRepository cart;
    private final CollectionService collection;

    public CartService(CartRepository cart,
                       CollectionService collection) {
        this.cart = cart;
        this.collection = collection;
    }

    // 장바구니 추가
    public synchronized CartItem add(long itemId, int quantity) {

        CollectionItem source = collection.findById(itemId);
        Cart cartDomain = new Cart(cart.findAll());

        CartItem item = cartDomain.add(source, quantity);

        if (item.getId() != 0) {
            cart.replace(item);
            return item;
        }

        return cart.saveNew(item);
    }

    // 전체 조회
    public List<CartItem> findAll() {
        return cart.findAll();
    }

    // 단건 조회
    public CartItem findById(long cartId) {
        return cart.findById(cartId)
                .orElseThrow(() ->
                        new NotFoundException(
                                "장바구니 항목을 찾을 수 없습니다."
                        )
                );
    }

    // 수량 변경
    public synchronized CartItem update(long cartId, int quantity) {

        Cart cartDomain = new Cart(cart.findAll());
        CartItem updated = cartDomain.update(cartId, quantity);

        cart.replace(updated);

        return updated;
    }

    // 삭제
    public synchronized void delete(long cartId) {
        findById(cartId);
        cart.delete(cartId);
    }

    // 최종 금액 미리보기
    public CartPreview preview(String couponCode) {

        Coupon coupon = Coupon.from(couponCode);
        Cart cartDomain = new Cart(cart.findAll());

        return cartDomain.preview(coupon);
    }
}
