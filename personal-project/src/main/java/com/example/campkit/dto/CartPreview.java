
package com.example.campkit.dto;

import com.example.campkit.domain.Coupon;

public record CartPreview(
        long subtotal,
        long discount,
        long discountedSubtotal,
        long shipping,
        long finalAmount,
        Coupon coupon
) {}
