package com.example.wishlist.product.client;

import com.example.wishlist.product.domain.ExternalProduct;
import com.example.wishlist.product.domain.ExternalProductPage;

// 외부 상품 API 호출하는 client
public interface ProductClient {

    ExternalProductPage findAll(int limit, int skip);

    ExternalProductPage search(String keyword, int limit, int skip);

    ExternalProduct findById(long id);

}
