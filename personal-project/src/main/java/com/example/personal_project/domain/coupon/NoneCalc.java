package com.example.personal_project.domain.coupon;

public class NoneCalc implements CouponCalc{
    @Override
    public Long calc(Long subtotal) {
        return 0L;
    }
}
