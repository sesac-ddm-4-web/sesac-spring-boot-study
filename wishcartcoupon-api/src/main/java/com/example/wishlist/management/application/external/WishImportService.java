package com.example.wishlist.management.application.external;

import com.example.wishlist.management.domain.external.ExternalPriceConverter;
import com.example.wishlist.management.domain.external.ExternalProduct;
import com.example.wishlist.management.domain.external.ExternalProductClient;
import com.example.wishlist.management.domain.wish.Wish;
import com.example.wishlist.management.domain.wish.WishRepository;
import com.example.wishlist.management.domain.exception.ConflictException;
import org.springframework.stereotype.Service;

@Service
public class WishImportService {
    // 각각 인터페이스
    private final ExternalProductClient externalProductClient;
    private final WishRepository wishRepository;

    public WishImportService(ExternalProductClient externalProductClient, WishRepository wishRepository) {
        this.externalProductClient = externalProductClient;
        this.wishRepository = wishRepository;
    }

    public Wish importWish(long externalId){
        // 중복 확인 (저장소 변경 없음) -> 409
        if(wishRepository.existByExternalId(externalId)){
            throw new ConflictException("이미 가져온 외부 상품입니다.");
        }
        // 외부 상세 조회 (실패해도 저장소 변경 없음) -> 404 / 502
        ExternalProduct product = externalProductClient.get(externalId);
        // 가격 환산
        int priceWon = ExternalPriceConverter.toWon(product.price());
        // 저장
        Wish wish = Wish.external(externalId, product.title(), priceWon, product.stock());
        return wishRepository.add(wish);
    }
}
