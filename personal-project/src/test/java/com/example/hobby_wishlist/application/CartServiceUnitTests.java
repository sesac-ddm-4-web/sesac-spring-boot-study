package com.example.hobby_wishlist.application;

import com.example.hobby_wishlist.domain.cart.Cart;
import com.example.hobby_wishlist.domain.cart.CartRepository;
import com.example.hobby_wishlist.domain.coupon.CouponNotApplicableException;
import com.example.hobby_wishlist.domain.coupon.CouponNotFoundException;
import com.example.hobby_wishlist.domain.wishlist.WishlistItem;
import com.example.hobby_wishlist.domain.wishlist.WishlistRepository;
import com.example.hobby_wishlist.dto.cart.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
    }

    @Test
    @DisplayName("장바구니의 모든 상품과 합계를 가져옴")
    void getCartTest() {
        when(cartRepository.getCart()).thenReturn(setUpCart());

        CartPreviewResponse response = cartService.getPreview("NONE");

        assertEquals(List.of(
                new CartItemResponse(1L, 1L, "의자", 25000, null, 5),
                new CartItemResponse(2L, 2L, "외부상품", 9999, 20, 3)), response.items());
        // 소계 125,000 + 29,997 = 154,997 → 쿠폰 없음, 10만원 이상이라 배송비 무료
        assertEquals("NONE", response.coupon());
        assertEquals(0L, response.couponDiscount());
        assertEquals(0L, response.shippingFee());
        assertEquals(154_997L, response.total());
    }

    @Test
    @DisplayName("PERCENT10 쿠폰: 소계의 10%가 할인됨")
    void previewPercentCouponTest() {
        when(cartRepository.getCart()).thenReturn(setUpCart()); // 소계 154,997

        CartPreviewResponse response = cartService.getPreview("PERCENT10");

        assertEquals("PERCENT10", response.coupon());
        assertEquals(15_499L, response.couponDiscount());
        assertEquals(0L, response.shippingFee());
        assertEquals(139_498L, response.total());
    }

    @Test
    @DisplayName("쿠폰 적용 후 금액이 10만원 미만이면 배송비가 붙음")
    void previewShippingAfterCouponTest() {
        Cart cart = cartOf(WishlistItem.fromManual(1L, "의자", 55_000), 2); // 소계 110,000
        when(cartRepository.getCart()).thenReturn(cart);

        CartPreviewResponse response = cartService.getPreview("FIXED15000");

        assertEquals(15_000L, response.couponDiscount());
        assertEquals(5_000L, response.shippingFee());
        assertEquals(100_000L, response.total()); // 95,000 + 5,000
    }

    @Test
    @DisplayName("쿠폰이 없고 소계가 10만원 미만이면 배송비가 붙음")
    void previewShippingWithoutCouponTest() {
        Cart cart = cartOf(WishlistItem.fromManual(1L, "연필", 500), 2); // 소계 1,000
        when(cartRepository.getCart()).thenReturn(cart);

        CartPreviewResponse response = cartService.getPreview(null);

        assertEquals("NONE", response.coupon());
        assertEquals(5_000L, response.shippingFee());
        assertEquals(6_000L, response.total());
    }

    @Test
    @DisplayName("빈 장바구니는 배송비만 계산됨")
    void previewEmptyCartTest() {
        when(cartRepository.getCart()).thenReturn(new Cart());

        CartPreviewResponse response = cartService.getPreview("");

        assertEquals(List.of(), response.items());
        assertEquals(5_000L, response.total());
    }

    @Test
    @DisplayName("존재하지 않는 쿠폰이면 CouponNotFoundException")
    void previewUnknownCouponTest() {
        when(cartRepository.getCart()).thenReturn(setUpCart());

        assertThrows(CouponNotFoundException.class, () -> cartService.getPreview("NOPE"));
    }

    @Test
    @DisplayName("적용 조건을 못 채운 쿠폰이면 CouponNotApplicableException")
    void previewNotApplicableCouponTest() {
        Cart cart = cartOf(WishlistItem.fromManual(1L, "연필", 500), 2); // 소계 1,000 < 100,000
        when(cartRepository.getCart()).thenReturn(cart);

        assertThrows(CouponNotApplicableException.class, () -> cartService.getPreview("FIXED15000"));
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
    }

    // 상품 하나만 담긴 장바구니 세팅
    private Cart cartOf(WishlistItem item, int quantity) {
        Cart cart = new Cart();
        cart.add(item, quantity);
        cart.assignIds(new AtomicLong(1L)::getAndIncrement);
        return cart;
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
