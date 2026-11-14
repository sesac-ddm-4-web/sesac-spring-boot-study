
package com.example.campkit.service;

import com.example.campkit.domain.*;
import com.example.campkit.external.DummyJsonProduct;
import com.example.campkit.exception.*;
import com.example.campkit.repository.CollectionRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.List;

@Service
public class CollectionService {

    private final CollectionRepository repository;
    private final ExternalProductService external;

    public CollectionService(CollectionRepository repository,
                             ExternalProductService external) {
        this.repository = repository;
        this.external = external;
    }



    // 기존 캠핑 장비 등록
    public CollectionItem register(String name, long price) {
        CollectionItem item = new CollectionItem(null, name, price);
        return repository.save(item);
    }

    // 기존 전체 조회
    public List<CollectionItem> findAll() {
        return repository.findAll();
    }

    // 기존 단건 조회
    public CollectionItem findById(Long id) {
        return repository.findById(id)
                .orElseThrow(() ->
                        new NotFoundException("해당 장비를 찾을 수 없습니다."));
    }

    // 기존 예산별 추천
    public List<CollectionItem> recommend(long budget) {
        if (budget < 0) {
            throw new IllegalArgumentException("예산은 0원 이상이어야 합니다.");
        }

        return repository.findAll().stream()
                .filter(item -> item.getPrice() <= budget)
                .sorted(Comparator.comparingLong(CollectionItem::getPrice))
                .limit(3)
                .toList();
    }

    // 신규: 외부 상품을 위시리스트로 가져오기
    public synchronized CollectionItem importExternal(long externalId) {

        if (externalId <= 0) {
            throw new IllegalArgumentException("외부 상품 ID가 올바르지 않습니다.");
        }

        // 중복 가져오기 방지
        if (repository.findByExternalId(externalId).isPresent()) {
            throw new ConflictException("이미 가져온 외부 상품입니다.");
        }

        // 서버에서 상세 정보를 조회
        DummyJsonProduct product = external.findById(externalId);

        // 외부 가격을 수업용 원화로 변환
        long won;

        try {
            won = product.price()
                    .multiply(BigDecimal.valueOf(1000))
                    .setScale(0, RoundingMode.HALF_UP)
                    .longValueExact();

        } catch (ArithmeticException e) {
            throw new ExternalApiException("외부 상품 가격이 너무 큽니다.", e);
        }

        CollectionItem item = new CollectionItem(
                null,
                product.title(),
                won,
                SourceType.DUMMYJSON,
                product.id(),
                product.stock()
        );

        return repository.save(item);
    }
    public void delete(Long id) {
        repository.findById(id)
                .orElseThrow(() ->
                        new NotFoundException("해당 장비를 찾을 수 없습니다.")
                );

        repository.delete(id);
    }
}
