package com.example.wishlistbudget.service;

import com.example.wishlistbudget.domain.CartItem;
import com.example.wishlistbudget.domain.CollectionItem;
import com.example.wishlistbudget.domain.CouponType;

import com.example.wishlistbudget.dto.CartItemResponse;
import com.example.wishlistbudget.dto.CartPreviewResponse;
import com.example.wishlistbudget.dto.CartResponse;

import com.example.wishlistbudget.exception.CartConflictException;
import com.example.wishlistbudget.exception.CartItemNotFoundException;
import com.example.wishlistbudget.exception.CollectionItemNotFoundException;
import com.example.wishlistbudget.exception.InvalidCartRequestException;

import com.example.wishlistbudget.repository.CartRepository;
import com.example.wishlistbudget.repository.CollectionRepository;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class CartService {

    private final CartRepository cartRepository;
    private final CollectionRepository collectionRepository;

    public CartService(
            CartRepository cartRepository,
            CollectionRepository collectionRepository
    ) {
        this.cartRepository = cartRepository;
        this.collectionRepository = collectionRepository;
    }

    // 장바구니 상품 추가
    public synchronized CartItem addItem(
            Long collectionItemId,
            int quantity
    ) {

        // 1. 요청 수량 검증
        if (quantity < 1 || quantity > 5) {
            throw new InvalidCartRequestException(
                    "수량은 1~5 사이여야 합니다."
            );
        }

        // 2. 이미 장바구니에 담긴 상품인지 확인
        CartItem existingItem = cartRepository
                .findByCollectionItemId(collectionItemId)
                .orElse(null);

        if (existingItem != null) {

            // 기존 수량과 새 수량 합산
            int newQuantity = existingItem.quantity() + quantity;

            // 상품별 최대 수량 검사
            if (newQuantity > 5) {
                throw new CartConflictException(
                        "상품별 최대 수량은 5개입니다."
                );
            }

            // 기존 재고 스냅샷 기준 검사
            Integer stock = existingItem.stock();

            if (stock != null && newQuantity > stock) {
                throw new CartConflictException(
                        "상품 재고를 초과했습니다."
                );
            }

            // 수량만 변경한 새로운 CartItem 생성
            CartItem updatedItem = new CartItem(
                    existingItem.id(),
                    existingItem.collectionItemId(),
                    existingItem.name(),
                    existingItem.price(),
                    existingItem.stock(),
                    newQuantity
            );

            return cartRepository.update(updatedItem);
        }

        // 3. 위시리스트에서 상품 조회
        CollectionItem collectionItem = collectionRepository
                .findById(collectionItemId)
                .orElseThrow(() ->
                        new CollectionItemNotFoundException(collectionItemId)
                );

        // 4. 장바구니 최대 20종류 검사
        List<CartItem> cartItems = cartRepository.findAll();

        if (cartItems.size() >= 20) {
            throw new CartConflictException(
                    "장바구니에는 최대 20종류의 상품만 담을 수 있습니다."
            );
        }

        // 5. 외부 상품 재고 검사
        Integer stock = collectionItem.stock();

        if (stock != null && quantity > stock) {
            throw new CartConflictException(
                    "상품 재고를 초과했습니다."
            );
        }

        // 6. 위시리스트 상품 정보를 복사해서 저장
        return cartRepository.save(
                collectionItem.id(),
                collectionItem.name(),
                collectionItem.price(),
                collectionItem.stock(),
                quantity
        );
    }

    // 장바구니 전체 조회
    public CartResponse getCart() {

        List<CartItem> cartItems = cartRepository.findAll();

        List<CartItemResponse> responses = new ArrayList<>();

        int totalQuantity = 0;
        long subtotal = 0L;

        for (CartItem item : cartItems) {

            responses.add(CartItemResponse.from(item));

            totalQuantity += item.quantity();

            subtotal += (long) item.price() * item.quantity();
        }

        return new CartResponse(
                responses,
                cartItems.size(),
                totalQuantity,
                subtotal
        );
    }

    // 장바구니 상품 수량 변경
    public synchronized CartItem updateQuantity(
            Long cartItemId,
            int quantity
    ) {

        // 1. 요청 수량 검증
        if (quantity < 1 || quantity > 5) {
            throw new InvalidCartRequestException(
                    "수량은 1~5 사이여야 합니다."
            );
        }

        // 2. 장바구니 상품 조회
        CartItem item = cartRepository
                .findById(cartItemId)
                .orElseThrow(() ->
                        new CartItemNotFoundException(cartItemId)
                );

        // 3. 기존 재고 스냅샷 기준 검사
        Integer stock = item.stock();

        if (stock != null && quantity > stock) {
            throw new CartConflictException(
                    "상품 재고를 초과했습니다."
            );
        }

        // 4. 수량만 변경한 새로운 객체 생성
        CartItem updatedItem = new CartItem(
                item.id(),
                item.collectionItemId(),
                item.name(),
                item.price(),
                item.stock(),
                quantity
        );

        // 5. 기존 객체 교체
        return cartRepository.update(updatedItem);
    }

    // 장바구니 상품 삭제
    public synchronized void deleteItem(Long cartItemId) {

        boolean deleted = cartRepository.deleteById(cartItemId);

        if (!deleted) {
            throw new CartItemNotFoundException(cartItemId);
        }
    }

    // 결제 예상 금액 미리보기
    public CartPreviewResponse preview(CouponType coupon) {

        // 1. 장바구니 조회
        CartResponse cart = getCart();

        long subtotal = cart.subtotal();

        // 2. 빈 장바구니 처리
        if (cart.itemCount() == 0) {

            if (coupon != CouponType.NONE) {
                throw new InvalidCartRequestException(
                        "빈 장바구니에는 쿠폰을 적용할 수 없습니다."
                );
            }

            return new CartPreviewResponse(
                    0L,
                    CouponType.NONE,
                    0L,
                    0L,
                    0L,
                    0L
            );
        }

        // 3. 할인 금액 계산
        long discount = 0L;

        if (coupon == CouponType.PERCENT10) {

            discount = subtotal / 10;

            if (discount > 20000L) {
                discount = 20000L;
            }

        } else if (coupon == CouponType.FIXED15000) {

            if (subtotal < 100000L) {
                throw new InvalidCartRequestException(
                        "15,000원 할인 쿠폰은 10만 원 이상부터 사용할 수 있습니다."
                );
            }

            discount = 15000L;
        }

        // 4. 할인 적용 후 상품 금액
        long discountedSubtotal = subtotal - discount;

        // 5. 배송비 계산
        long shippingFee = 0L;

        if (discountedSubtotal < 100000L) {
            shippingFee = 5000L;
        }

        // 6. 최종 결제 예상 금액
        long finalTotal = discountedSubtotal + shippingFee;

        return new CartPreviewResponse(
                subtotal,
                coupon,
                discount,
                discountedSubtotal,
                shippingFee,
                finalTotal
        );
    }
}