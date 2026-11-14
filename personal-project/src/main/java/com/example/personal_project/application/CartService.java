package com.example.personal_project.application;

import com.example.personal_project.domain.*;
import com.example.personal_project.presentation.CartItemResponse;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CartService {
    private final ItemRepository itemRepository;
    private final CartRepository cartRepository;

    public CartService(ItemRepository itemRepository, CartRepository cartRepository) {
        this.itemRepository = itemRepository;
        this.cartRepository = cartRepository;
    }

    public CartItemResponse add(Long itemId, int quantity) {
//        AddCartItemRequest
//        “10번 상품 2개 담아 줘”
//        ↓
//        CartService
//        상품을 찾고 → 장바구니를 받아 → 담기를 요청
//        ↓
//        Cart
//        규칙을 확인하고 CartItem을 목록에 보관
//        ↓
//        CartItemResponse
//        “장바구니 1번 행에 노트 2개를 담았어”
        // Service가 ItemRepository에서 기존 Item을 받아.
        Item item = itemRepository.findById(itemId);
//        Item
//        id: 10
//        name: 노트
//        price: 12000

        // 보관 중인 Cart에 담기를 부탁해.
        Cart cart = cartRepository.getCart();

        // 장바구니에 담고, 추가 또는 수량 합산된 행을 반환받음
        CartItem savedItem = cart.add(item, quantity);
//        CartItem
//        id: 1          ← 새 장바구니 행 번호
//        itemId: 10     ← 원본 Item의 id를 복사
//        name: 노트     ← 복사
//        unitPrice: 12000 ← 복사
//        quantity: 2    ← 요청에서 받은 수량

//        위시리스트 저장소
//        └── Item: 10번 노트, 12000원
//        장바구니 저장소의 Cart
//        └── CartItem: 1번 행, 원본 10번, 노트, 12000원, 2개
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
}
