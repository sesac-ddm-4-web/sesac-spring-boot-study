package com.example.wishlistbudget.service;

import com.example.wishlistbudget.domain.CartItem;
import com.example.wishlistbudget.domain.CollectionItem;
import com.example.wishlistbudget.domain.CouponType;
import com.example.wishlistbudget.dto.CartPreviewResponse;
import com.example.wishlistbudget.dto.CartResponse;
import com.example.wishlistbudget.exception.CartConflictException;
import com.example.wishlistbudget.repository.CartRepository;
import com.example.wishlistbudget.repository.CollectionRepository;
import com.example.wishlistbudget.repository.MemoryCartRepository;
import com.example.wishlistbudget.repository.MemoryCollectionRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CartServiceTest {

    private CartService service;
    private CollectionRepository collectionRepository;

    @BeforeEach
    void setUp() {
        CartRepository cartRepository = new MemoryCartRepository();
        collectionRepository = new MemoryCollectionRepository();

        service = new CartService(cartRepository, collectionRepository);
    }

    @Test
    @DisplayName("위시리스트 상품을 장바구니에 정상 등록한다")
    void addItem_success() {

        CollectionItem product =
                collectionRepository.save("운동화", 50000);

        CartItem result = service.addItem(product.id(), 2);

        assertEquals("운동화", result.name());
        assertEquals(2, result.quantity());
        assertEquals(50000, result.price());
        assertEquals(100000, service.getCart().subtotal());
    }

    @Test
    @DisplayName("동일한 상품을 추가하면 수량이 합산된다")
    void addItem_duplicate() {

        CollectionItem product =
                collectionRepository.save("운동화", 50000);

        service.addItem(product.id(), 2);
        service.addItem(product.id(), 2);

        CartResponse result = service.getCart();

        assertEquals(1, result.itemCount());
        assertEquals(4, result.totalQuantity());
        assertEquals(200000, result.subtotal());
    }

    @Test
    @DisplayName("합산 수량이 5개를 초과하면 기존 수량을 유지한다")
    void addItem_exceedsMaxQuantity() {

        CollectionItem product =
                collectionRepository.save("운동화", 50000);

        service.addItem(product.id(), 4);

        assertThrows(
                CartConflictException.class,
                () -> service.addItem(product.id(), 2)
        );

        assertEquals(4, service.getCart().totalQuantity());
    }

    @Test
    @DisplayName("외부 상품의 재고보다 많은 수량은 추가할 수 없다")
    void addItem_exceedsStock() {

        CollectionItem product =
                collectionRepository.saveExternal(
                        "운동화", 50000, 100L, 2
                );

        assertThrows(
                CartConflictException.class,
                () -> service.addItem(product.id(), 3)
        );

        assertEquals(0, service.getCart().itemCount());
    }

    @Test
    @DisplayName("재고가 0인 상품은 장바구니에 추가할 수 없다")
    void addItem_outOfStock() {

        CollectionItem product =
                collectionRepository.saveExternal(
                        "품절 운동화", 30000, 200L, 0
                );

        assertThrows(
                CartConflictException.class,
                () -> service.addItem(product.id(), 1)
        );

        assertEquals(0, service.getCart().itemCount());
    }

    @Test
    @DisplayName("장바구니 상품의 수량을 변경할 수 있다")
    void updateQuantity_success() {

        CollectionItem product =
                collectionRepository.save("운동화", 50000);

        CartItem item = service.addItem(product.id(), 2);

        CartItem updated = service.updateQuantity(item.id(), 3);

        assertEquals(3, updated.quantity());
        assertEquals(150000, service.getCart().subtotal());
    }

    @Test
    @DisplayName("장바구니 상품을 삭제할 수 있다")
    void deleteItem_success() {

        CollectionItem product =
                collectionRepository.save("운동화", 50000);

        CartItem item = service.addItem(product.id(), 2);

        service.deleteItem(item.id());

        assertEquals(0, service.getCart().itemCount());
        assertEquals(0, service.getCart().subtotal());
    }

    @Test
    @DisplayName("장바구니에는 최대 20종류의 상품만 등록할 수 있다")
    void addItem_exceedsMaxItems() {

        for (int i = 1; i <= 20; i++) {
            CollectionItem product =
                    collectionRepository.save("상품" + i, 1000);

            service.addItem(product.id(), 1);
        }

        CollectionItem extraProduct =
                collectionRepository.save("추가 상품", 1000);

        assertThrows(
                CartConflictException.class,
                () -> service.addItem(extraProduct.id(), 1)
        );

        assertEquals(20, service.getCart().itemCount());
    }

    @Test
    @DisplayName("10% 쿠폰 적용 후 금액을 기준으로 배송비를 계산한다")
    void preview_percent10() {

        CollectionItem product =
                collectionRepository.save("운동화", 50000);

        service.addItem(product.id(), 2);

        CartPreviewResponse result =
                service.preview(CouponType.PERCENT10);

        assertEquals(100000, result.subtotal());
        assertEquals(10000, result.discount());
        assertEquals(90000, result.discountedSubtotal());
        assertEquals(5000, result.shippingFee());
        assertEquals(95000, result.finalTotal());
    }

    @Test
    @DisplayName("15,000원 쿠폰 적용 후 배송비를 계산한다")
    void preview_fixed15000() {

        CollectionItem product =
                collectionRepository.save("운동화", 50000);

        service.addItem(product.id(), 2);

        CartPreviewResponse result =
                service.preview(CouponType.FIXED15000);

        assertEquals(100000, result.subtotal());
        assertEquals(15000, result.discount());
        assertEquals(85000, result.discountedSubtotal());
        assertEquals(5000, result.shippingFee());
        assertEquals(90000, result.finalTotal());
    }

    @Test
    @DisplayName("할인 후 금액이 10만 원 이상이면 배송비가 무료다")
    void preview_freeShipping() {

        CollectionItem product =
                collectionRepository.save("운동화", 60000);

        service.addItem(product.id(), 2);

        CartPreviewResponse result =
                service.preview(CouponType.NONE);

        assertEquals(120000, result.subtotal());
        assertEquals(0, result.discount());
        assertEquals(0, result.shippingFee());
        assertEquals(120000, result.finalTotal());
    }

    @Test
    @DisplayName("빈 장바구니의 모든 금액은 0이다")
    void preview_emptyCart() {

        CartPreviewResponse result =
                service.preview(CouponType.NONE);

        assertEquals(0, result.subtotal());
        assertEquals(0, result.discount());
        assertEquals(0, result.discountedSubtotal());
        assertEquals(0, result.shippingFee());
        assertEquals(0, result.finalTotal());
    }

    @Test
    @DisplayName("동일 상품을 다시 추가해도 최초 상품 정보가 유지된다")
    void addItem_keepsSnapshot() {

        CollectionItem product =
                collectionRepository.saveExternal(
                        "운동화", 50000, 100L, 5
                );

        CartItem first = service.addItem(product.id(), 1);
        CartItem updated = service.addItem(product.id(), 2);

        assertEquals(first.id(), updated.id());
        assertEquals(first.name(), updated.name());
        assertEquals(first.price(), updated.price());
        assertEquals(first.stock(), updated.stock());
        assertEquals(3, updated.quantity());
    }
}