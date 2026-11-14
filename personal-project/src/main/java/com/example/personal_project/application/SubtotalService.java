package com.example.personal_project.application;

import com.example.personal_project.domain.coupon.CouponCode;
import com.example.personal_project.domain.exception.CommonBadRequestException;
import com.example.personal_project.domain.repository.ShoppingCartRepository;
import com.example.personal_project.presentation.dto.shoppingCart.SubtotalRequest;
import com.example.personal_project.presentation.dto.shoppingCart.SubtotalResponse;
import org.springframework.stereotype.Service;

@Service
public class SubtotalService {
    private final ShoppingCartRepository shoppingCartRepository;

    public SubtotalService(ShoppingCartRepository shoppingCartRepository) {
        this.shoppingCartRepository = shoppingCartRepository;
    }

    public SubtotalResponse getSubtotal(SubtotalRequest subtotalRequest){
        CouponCode couponCode = CouponCode.createCouponCode(subtotalRequest.couponCode());
        Long subtotal = CouponCode.calcSubtotal(shoppingCartRepository.findAll());
        Long discount = couponCode.discount(subtotal);
        Long discountedSubtotal = subtotal - discount;
        Integer shipping = couponCode.getShipping(discountedSubtotal);
        Long finalPrice = discountedSubtotal + shipping;
        String coupon = subtotalRequest.couponCode();

        return new SubtotalResponse(subtotal,discount,discountedSubtotal,shipping,finalPrice,coupon);
    }
}
