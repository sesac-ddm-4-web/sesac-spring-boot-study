package com.example.personal_project.domain.coupon;

import com.example.personal_project.domain.exception.CommonBadRequestException;

public class Percent10Calc implements CouponCalc{
    private final long MAX_DISCOUNT = 20000L;
    @Override
    public Long calc(Long subtotal) {
        if(subtotal<=0)
            throw new CommonBadRequestException("가격이 0원 이하입니다");
        return Math.min((long) (subtotal.doubleValue()*0.1),MAX_DISCOUNT);
    }
}
