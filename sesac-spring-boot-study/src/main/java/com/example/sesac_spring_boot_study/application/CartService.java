package com.example.sesac_spring_boot_study.application;

import com.example.sesac_spring_boot_study.domain.*;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CartService {
    private CartRepository cartRepository;
    private ProductRepository productRepository;

    public CartService(CartRepository cartRepository, ProductRepository productRepository){
        this.cartRepository = cartRepository;
        this.productRepository = productRepository;
    }

    public CartItem addItem(Long productId, int quantity){
        if(productId==null){
            throw new IllegalArgumentException("상품 id는 필수입니다.");
        }

        Product product = productRepository.findById(productId);

        return cartRepository.getCart().add(product.getId(), product.getName(), product.getPrice(), product.getStock(), quantity);
    }

    public List<CartItem> getItems() {
        return cartRepository.getCart().getItems();
    }

    public CartItem changeQuantity(Long rowId, int quantity){
        return cartRepository.getCart().changeQuantity(rowId, quantity);
    }

    public void remove (Long rowId){
        cartRepository.getCart().remove(rowId);
    }

    public CartPreview preview(String couponCode) {
        return cartRepository.getCart().preview(Coupon.from(couponCode));
    }


}
