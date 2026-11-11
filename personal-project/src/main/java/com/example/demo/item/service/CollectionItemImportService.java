package com.example.demo.item.service;

import com.example.demo.externalproduct.client.ExternalProductClient;
import com.example.demo.externalproduct.client.dto.DummyJsonProduct;
import com.example.demo.global.exception.ConflictException;
import com.example.demo.item.domain.CollectionItem;
import com.example.demo.item.domain.PriceConverter;
import com.example.demo.item.dto.req.ExternalImportReq;
import com.example.demo.item.dto.res.CollectionItemRes;
import com.example.demo.item.repository.CollectionItemRepository;
import org.springframework.stereotype.Service;

// 외부 상품을 내 위시로
// 입력 검증, 중복 확인, 외부 조회, 가격 환산, 도메인 생성, 저장
@Service
public class CollectionItemImportService {

    private final CollectionItemRepository collectionItemRepository;
    private final ExternalProductClient externalProductClient;
    private final ValidationService validationService;

    public CollectionItemImportService(CollectionItemRepository collectionItemRepository,
                                       ExternalProductClient externalProductClient,
                                       ValidationService validationService) {
        this.collectionItemRepository = collectionItemRepository;
        this.externalProductClient = externalProductClient;
        this.validationService = validationService;
    }

    // synchronized로 같은 상품이 동시에 두 번 저장되는 것을 막음
    public synchronized CollectionItemRes importExternal(ExternalImportReq request) {
        validationService.checkValid(request);

        if (collectionItemRepository.existsByExternalId(request.externalId())) {
            throw new ConflictException("이미 가져온 외부 상품입니다.");
        }

        DummyJsonProduct product = externalProductClient.findById(request.externalId());

        int priceWon = PriceConverter.toWon(product.price());

        CollectionItem item = CollectionItem.ofExternal(
                product.id(), product.title(), priceWon, product.stock());
        validationService.checkValid(item);

        CollectionItem saved = collectionItemRepository.add(item);
        return CollectionItemRes.from(saved);
    }
}
