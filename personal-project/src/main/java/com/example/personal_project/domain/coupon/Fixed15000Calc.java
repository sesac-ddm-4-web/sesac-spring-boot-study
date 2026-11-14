package com.example.personal_project.domain.coupon;

import com.example.personal_project.domain.exception.CommonBadRequestException;

public class Fixed15000Calc implements CouponCalc{
    private final long MIN_SUBTOTAL = 100000L;
    @Override
    public Long calc(Long subtotal) {
        if(subtotal<MIN_SUBTOTAL) throw new CommonBadRequestException(MIN_SUBTOTAL+"원 이상부터 사용 가능한 쿠폰입니다.");
        return 15000L;
    }
}
