package com.example.hobby_wishlist.application;

import com.example.hobby_wishlist.domain.cart.Cart;
import com.example.hobby_wishlist.domain.cart.CartRepository;
import com.example.hobby_wishlist.domain.wishlist.WishlistItem;
import com.example.hobby_wishlist.domain.wishlist.WishlistRepository;
import com.example.hobby_wishlist.dto.cart.AddCartItemRequest;
import com.example.hobby_wishlist.dto.cart.CartItemResponse;
import com.example.hobby_wishlist.dto.cart.CartResponse;
import com.example.hobby_wishlist.dto.cart.ChangeCartItemQuantityRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CartServiceUnitTests {

    @Mock
    private CartRepository cartRepository;

    @Mock
    private WishlistRepository wishlistRepository;

    @InjectMocks
    private CartService cartService;

    @Test
    @DisplayName("위시리스트 상품을 장바구니에 추가하면 장바구니 전체를 돌려줌")
    void addItemTest() {
        sequence = new AtomicLong(1L);
        when(wishlistRepository.findById(1L)).thenReturn(WishlistItem.fromManual(1L, "의자", 25000));
        when(cartRepository.getCart()).thenReturn(new Cart());
        when(cartRepository.saveChanges(any())).thenAnswer(invocation -> {
            Cart cart = invocation.getArgument(0);
            cart.assignIds(sequence::getAndIncrement);
            return cart;
        });

        CartResponse response = cartService.addItem(new AddCartItemRequest(1L, 2));

        assertEquals(List.of(new CartItemResponse(1L, 1L, "의자", 25000, null, 2)), response.items());
        assertEquals(50000L, response.subtotal());
    }

    @Test
    @DisplayName("장바구니의 모든 상품과 합계를 가져옴")
    void getCartTest() {
        when(cartRepository.getCart()).thenReturn(setUpCart());

        CartResponse response = cartService.getAllItems();

        assertEquals(List.of(
                new CartItemResponse(1L, 1L, "의자", 25000, null, 5),
                new CartItemResponse(2L, 2L, "외부상품", 9999, 20, 3)), response.items());
        assertEquals(25000L * 5 + 9999L * 3, response.subtotal());
    }

    @Test
    @DisplayName("수량을 변경하면 변경된 장바구니를 돌려줌")
    void changeQuantityTest() {
        when(cartRepository.getCart()).thenReturn(setUpCart());
        when(cartRepository.saveChanges(any())).thenAnswer(invocation -> {
            Cart cart = invocation.getArgument(0);
            cart.assignIds(sequence::getAndIncrement);
            return cart;
        });

        CartResponse response = cartService.changeQuantity(2L, new ChangeCartItemQuantityRequest(1));

        assertEquals(1, response.items().get(1).quantity());
        assertEquals(25000L * 5 + 9999L, response.subtotal());
    }

    @Test
    @DisplayName("항목을 삭제하면 나머지만 남은 장바구니를 돌려줌")
    void removeItemTest() {
        when(cartRepository.getCart()).thenReturn(setUpCart());
        when(cartRepository.saveChanges(any())).thenAnswer(invocation -> {
            Cart cart = invocation.getArgument(0);
            cart.assignIds(sequence::getAndIncrement);
            return cart;
        });

        CartResponse response = cartService.removeItem(1L);

        assertEquals(1, response.items().size());
        assertEquals(9999L * 3, response.subtotal());
    }

    // sequence는 각 테스트마다 초기화
    private AtomicLong sequence;
    // 상품이 채워진 장바구니 세팅
    private Cart setUpCart() {
        sequence = new AtomicLong(1L);
        WishlistItem wishlistItem1 = WishlistItem.fromManual(1L, "의자", 25000);
        WishlistItem wishlistItem2 = WishlistItem.fromExternal(2L, 10L, "외부상품", 9999, 20);

        Cart cart = new Cart();
        cart.add(wishlistItem1, 5);
        cart.add(wishlistItem2, 3);
        cart.assignIds(sequence::getAndIncrement);

        return cart;
    }
}
