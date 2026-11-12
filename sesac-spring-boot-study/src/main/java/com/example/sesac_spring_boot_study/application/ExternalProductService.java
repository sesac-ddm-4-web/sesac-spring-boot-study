package com.example.sesac_spring_boot_study.application;

import com.example.sesac_spring_boot_study.domain.Product;
import com.example.sesac_spring_boot_study.domain.ProductRepository;
import com.example.sesac_spring_boot_study.domain.SourceType;
import com.example.sesac_spring_boot_study.product.DummyJsonClient;
import com.example.sesac_spring_boot_study.product.DummyJsonProduct;
import com.example.sesac_spring_boot_study.product.PriceConverter;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ExternalProductService {
    private final DummyJsonClient client;
    private final ProductRepository productRepository;

    public ExternalProductService(DummyJsonClient client, ProductRepository productRepository) {
        this.client = client;
        this.productRepository = productRepository;
    }

    public Product importProduct(Long externalId) {
        if (externalId == null || externalId <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "외부 상품 id는 양수여야 합니다");
        }

        if (productRepository.existsByExternalId(SourceType.EXTERNAL, externalId)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "이미 가져온 상품입니다");
        }

        DummyJsonProduct external = client.findById(externalId);

        long won = PriceConverter.toWon(external.price());

        Product product = new Product(
                null,
                external.title(),
                Math.toIntExact(won),
                SourceType.EXTERNAL,
                external.id(),
                external.stock());
        return productRepository.add(product);
    }


}
