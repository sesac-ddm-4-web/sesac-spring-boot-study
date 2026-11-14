package com.example.wishlist.application;

import com.example.wishlist.domain.CollectionItem;
import com.example.wishlist.domain.CollectionItemRepository;
import com.example.wishlist.domain.DuplicateItemException;
import com.example.wishlist.domain.ExternalProduct;
import com.example.wishlist.domain.PriceConverter;
import com.example.wishlist.domain.SourceType;
import com.example.wishlist.infrastructure.DummyJsonClient;
import com.example.wishlist.infrastructure.DummyJsonProduct;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class ExternalProductService {
    private final DummyJsonClient client;
    private final CollectionItemRepository repository;

    public ExternalProductService(DummyJsonClient client, CollectionItemRepository repository) {
        this.client = client;
        this.repository = repository;
    }

    public List<ExternalProduct> findAll(int limit, int skip) {
        return toProducts(client.findAll(limit, skip));
    }

    public List<ExternalProduct> search(String q, int limit, int skip) {
        return toProducts(client.search(q.strip(), limit, skip));
    }

    public ExternalProduct findById(long id) {
        return toProduct(client.findById(id));
    }

    public CollectionItem importToWishlist(long externalId) {
        if (repository.findByExternalId(SourceType.DUMMYJSON, externalId) != null) {
            throw new DuplicateItemException(externalId);
        }

        ExternalProduct product = toProduct(client.findById(externalId));

        int stock = product.stock() == null ? 0 : product.stock();
        CollectionItem item = new CollectionItem(
                product.title().strip(),
                product.price(),
                SourceType.DUMMYJSON,
                externalId,
                stock);
        return repository.add(item);
    }

    private List<ExternalProduct> toProducts(List<DummyJsonProduct> products) {
        return products.stream()
                .map(this::toProduct)
                .toList();
    }

    private ExternalProduct toProduct(DummyJsonProduct product) {
        if (product.title() == null || product.title().isBlank() || product.price() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY, "외부 상품의 이름이나 가격이 비어 있습니다");
        }
        return new ExternalProduct(
                product.id(),
                product.title(),
                PriceConverter.toWon(product.price()),
                product.category(),
                product.thumbnail(),
                product.stock());
    }
}
