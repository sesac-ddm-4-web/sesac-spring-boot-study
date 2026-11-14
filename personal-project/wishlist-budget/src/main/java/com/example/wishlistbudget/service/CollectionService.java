
package com.example.wishlistbudget.service;

import com.example.wishlistbudget.domain.CollectionItem;
import com.example.wishlistbudget.domain.SourceType;
import com.example.wishlistbudget.exception.CollectionItemNotFoundException;
import com.example.wishlistbudget.exception.DuplicateExternalProductException;
import com.example.wishlistbudget.external.client.DummyJsonClient;
import com.example.wishlistbudget.external.dto.DummyJsonProduct;
import com.example.wishlistbudget.repository.CollectionRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class CollectionService {

    private final CollectionRepository repository;
    private final DummyJsonClient dummyJsonClient;

    public CollectionService(CollectionRepository repository, DummyJsonClient dummyJsonClient) {
        this.repository = repository;
        this.dummyJsonClient = dummyJsonClient;
    }

    // 아이템 등록
    public CollectionItem addItem(String name, int price) {

        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("아이템 이름은 필수입니다.");
        }

        if (price < 0) {
            throw new IllegalArgumentException("가격은 0 이상이어야 합니다.");
        }

        // trim()으로 문자열 앞뒤 공백 제거
        String trimmedName = name.trim();

        return repository.save(trimmedName, price);
    }

    // 전체 아이템 조회
    public List<CollectionItem> getAllItems() {
        return repository.findAll();
    }

    // ID로 아이템 조회
    public CollectionItem getItem(Long id) {

        return repository.findById(id)
                .orElseThrow(() -> new CollectionItemNotFoundException(id));
    }

    // 예산 이하의 아이템 최대 3개 추천
    public List<CollectionItem> recommend(int budget) {

        if (budget < 0) {
            throw new IllegalArgumentException("예산은 0 이상이어야 합니다.");
        }

        List<CollectionItem> items = repository.findAll();

        List<CollectionItem> availableItems = new ArrayList<>();

        // 예산 이하 아이템만 선택
        for (CollectionItem item : items) {

            if (item.price() <= budget) {
                availableItems.add(item);
            }
        }

        // 가격 오름차순 정렬
        availableItems.sort(
                Comparator.comparingInt(CollectionItem::price)
        );

        // 최대 3개 추천
        List<CollectionItem> recommendations = new ArrayList<>();

        for (int i = 0; i < availableItems.size() && i < 3; i++) {
            recommendations.add(availableItems.get(i));
        }

        return recommendations;
    }

    // 외부 상품을 위시리스트에 등록
    public CollectionItem importProduct(Long externalId) {

        // 외부 상품 ID 검증
        if (externalId == null || externalId < 1) {
            throw new IllegalArgumentException("외부 상품 ID는 1 이상이어야 합니다.");
        }

        // 이미 등록된 외부 상품인지 중복 확인
        if (repository.existsBySourceTypeAndExternalId(
                SourceType.EXTERNAL, externalId)) {
            throw new DuplicateExternalProductException(externalId);
        }

        // DummyJSON API에서 외부 상품 상세 정보 조회
        DummyJsonProduct product = dummyJsonClient.getProduct(externalId);

        // 외부 상품 가격을 원화로 변환 (가격 × 1000, HALF_UP 반올림)
        BigDecimal convertedPrice = product.price()
                .multiply(BigDecimal.valueOf(1000))
                .setScale(0, RoundingMode.HALF_UP);

        // 현재 CollectionItem의 price 타입이 int이므로 범위 확인 후 변환
        int price = convertedPrice.intValueExact();

        // 외부 상품을 위시리스트에 저장
        // 품절 상품(stock = 0)도 저장 가능
        return repository.saveExternal(
                product.title(),
                price,
                product.id(),
                product.stock()
        );
    }
}
