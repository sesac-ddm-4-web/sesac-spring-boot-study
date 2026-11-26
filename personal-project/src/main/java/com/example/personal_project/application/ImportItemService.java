package com.example.personal_project.application;

import com.example.personal_project.application.port.ProductClient;
import com.example.personal_project.domain.item.model.CollectionItem;
import com.example.personal_project.domain.item.model.SourceType;
import com.example.personal_project.domain.item.repository.ItemRepository;
import com.example.personal_project.domain.product.model.ExternalProduct;
import com.example.personal_project.domain.product.model.PriceConverter;
import com.example.personal_project.global.exception.DuplicationExternalProductException;
import org.springframework.stereotype.Service;

@Service
public class ImportItemService {

    private final ProductClient productClient;
    private final ItemRepository itemRepository;
    private final PriceConverter priceConverter;

    public ImportItemService(
            ProductClient productClient,
            ItemRepository itemRepository,
            PriceConverter priceConverter
    ) {
        this.productClient = productClient;
        this.itemRepository = itemRepository;
        this.priceConverter = priceConverter;
    }

    public CollectionItem importProduct(Long externId) {
        if (externId == null || externId <= 0) {
            throw new IllegalArgumentException("외부 상품은 양수여야 합니다.");
        }

        SourceType sourceType = SourceType.DUMMY_JSON;

        if (itemRepository.existsBySourceTypeAndExternalId(sourceType, externId)) {
            throw new DuplicationExternalProductException();
        }

        ExternalProduct product = productClient.findById(externId);

        long price = priceConverter.toWon(product.price());

        CollectionItem item = new CollectionItem(
                null,
                product.title(),
                price,
                sourceType,
                product.externalId(),
                product.stock(),
                product.category(),
                product.thumbnail()
        );
        return itemRepository.add(item);
    }
}
