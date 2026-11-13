package com.example.wishlist.item.service;

import com.example.wishlist.item.domain.WishlistItem;
import com.example.wishlist.item.exception.DuplicateImportException;
import com.example.wishlist.item.repository.WishlistItemRepository;
import com.example.wishlist.product.client.ProductClient;
import com.example.wishlist.product.domain.ExternalProduct;
import org.springframework.stereotype.Service;

@Service
public class WishlistImportService {

    private final WishlistItemRepository repository;
    private final ProductClient client;

    public WishlistImportService(WishlistItemRepository repository, ProductClient productClient) {
        this.repository = repository;
        this.client = productClient;
    }

    public WishlistItem importProduct(long externalId) {
        if (repository.findByExternalId(externalId).isPresent()) {
            throw new DuplicateImportException(externalId);
        }

        ExternalProduct product = client.findById(externalId);

        WishlistItem item = WishlistItem.imported(
            externalId,
            product.title(),
            product.priceInWon(),
            product.stock()
        );
        return repository.save(item);
    }

}
