package com.example.personal_project.application;

import com.example.personal_project.domain.*;
import com.example.personal_project.presentation.CartItemResponse;
import com.example.personal_project.presentation.CartPreviewResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CartService {
    private final ItemRepository itemRepository;
    private final CartRepository cartRepository;

    public CartService(ItemRepository itemRepository, CartRepository cartRepository) {
        this.itemRepository = itemRepository;
        this.cartRepository = cartRepository;
    }

    @Transactional
    public CartItemResponse add(Long itemId, int quantity) {
        // Service가 ItemRepository에서 기존 Item을 받아.
        Item item = itemRepository.findById(itemId);
        Cart cart = cartRepository.getCart();

        // 장바구니에 담고, 추가 또는 수량 합산된 행을 반환받음
        CartItem savedItem = cart.add(item, quantity);
        cartRepository.save(cart);

        return CartItemResponse.from(savedItem);
    }

    public List<CartItemResponse> getItems() {
        // 장바구니 받기
        Cart cart = cartRepository.getCart();

        // 담긴 항목 목록 받기
        List<CartItem> items = cart.getItems();

        // 응답 DTO목록으로 변환
        List<CartItemResponse> responses = items.stream()
                .map(cartItem -> CartItemResponse.from(cartItem))
                .toList();

        return responses;
    }

    @Transactional
    public CartItemResponse changeQuantity(Long id, int quantity) {
        Cart cart = cartRepository.getCart();

        CartItem updatedItem = cart.changeQuantity(id, quantity);
        cartRepository.save(cart);

        return CartItemResponse.from(updatedItem);
    }

    @Transactional
    public void remove(Long id) {
        Cart cart = cartRepository.getCart();

        cart.remove(id);
        cartRepository.save(cart);
    }

    public CartPreviewResponse preview(String couponCode) {
        Coupon coupon;
        // 쿠폰 확인
        try {
            coupon = Coupon.valueOf(couponCode);
        } catch (IllegalArgumentException | NullPointerException exception) {
            throw new IllegalArgumentException("사용할 수 없는 쿠폰 코드입니다.");
        }
        // 저장소에서 Cart 받기
        Cart cart = cartRepository.getCart();
        // 금액 계산
        CartPreview preview = cart.preview(coupon);
        // 응답DTO 변환
        CartPreviewResponse response = CartPreviewResponse.from(preview);
        return response;
    }
}
