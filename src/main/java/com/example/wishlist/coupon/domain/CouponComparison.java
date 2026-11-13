package com.example.wishlist.coupon.domain;

import java.util.ArrayList;
import java.util.List;

// 최저 금액 쿠폰 찾기
public record CouponComparison(PriceBreakdown best, List<PriceBreakdown> candidates) {

    public CouponComparison {
        candidates = List.copyOf(candidates);
    }

    public static CouponComparison of(long subtotal) {
        List<PriceBreakdown> candidates = new ArrayList<>();
        for (Coupon coupon : Coupon.values()) {          // 선언 순서대로
            if (coupon == Coupon.NONE || (subtotal > 0 && coupon.isApplicableTo(subtotal))) {
                candidates.add(PriceBreakdown.calculate(subtotal, coupon));
            }
        }

        PriceBreakdown best = candidates.get(0);          // NONE은 항상 첫 번째 후보
        for (PriceBreakdown candidate : candidates) {
            if (candidate.finalAmount() < best.finalAmount()) {
                // "더 작을 때만" 교체 → 같으면 앞 순서 유지
                best = candidate;
            }
        }
        return new CouponComparison(best, candidates);
    }
}