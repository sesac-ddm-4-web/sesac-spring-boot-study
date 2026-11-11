package com.example.demo.cart.service;

import com.example.demo.cart.domain.Cart;
import com.example.demo.cart.domain.Coupon;
import com.example.demo.cart.dto.req.CartAddReq;
import com.example.demo.cart.dto.req.CartQuantityReq;
import com.example.demo.cart.dto.res.CartRes;
import com.example.demo.cart.dto.res.PricePreviewRes;
import com.example.demo.cart.repository.CartRepository;
import com.example.demo.item.domain.CollectionItem;
import com.example.demo.item.repository.CollectionItemRepository;
import com.example.demo.item.service.ValidationService;
import org.springframework.stereotype.Service;

// 장바구니가 하나이기에 동시에 들어온 요청들이 섞이지 않도록 모든 메서드를 synchronized로 둠
@Service
public class CartService {

    private final CartRepository cartRepository;
    private final CollectionItemRepository collectionItemRepository;
    private final ValidationService validationService;

    public CartService(CartRepository cartRepository,
                       CollectionItemRepository collectionItemRepository,
                       ValidationService validationService) {
        this.cartRepository = cartRepository;
        this.collectionItemRepository = collectionItemRepository;
        this.validationService = validationService;
    }

    public synchronized CartRes add(CartAddReq request) {
        validationService.checkValid(request);
        CollectionItem item = collectionItemRepository.findById(request.itemId());
        Cart cart = cartRepository.find();
        cart.add(item, request.quantity());
        return CartRes.from(cart);
    }

    public synchronized CartRes find() {
        return CartRes.from(cartRepository.find());
    }

    public synchronized CartRes changeQuantity(Long cartItemId, CartQuantityReq request) {
        validationService.checkValid(request);
        Cart cart = cartRepository.find();
        cart.changeQuantity(cartItemId, request.quantity());
        return CartRes.from(cart);
    }

    public synchronized void remove(Long cartItemId) {
        cartRepository.find().remove(cartItemId);
    }

    public synchronized PricePreviewRes preview(String couponCode) {
        Coupon coupon = Coupon.from(couponCode);
        return PricePreviewRes.from(cartRepository.find().preview(coupon));
    }
}
