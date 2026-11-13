package com.example.sesac_spring_boot_study.application;

import com.example.sesac_spring_boot_study.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CartService {
    private CartRepository cartRepository;
    private ProductRepository productRepository;

    public CartService(CartRepository cartRepository, ProductRepository productRepository){
        this.cartRepository = cartRepository;
        this.productRepository = productRepository;
    }
    @Transactional
    public CartItem addItem(Long productId, int quantity){
        if(productId==null){
            throw new IllegalArgumentException("상품 id는 필수입니다.");
        }

        Product product = productRepository.findById(productId);
        Cart cart = new Cart(cartRepository.findAll());
        CartItem item = cart.add(product.getId(), product.getName(), product.getPrice(), product.getStock(), quantity);

        if (item.getRowId() == null) {
            return cartRepository.add(item);
        }
        return cartRepository.update(item);

    }
    @Transactional
    public List<CartItem> getItems() {
        Cart cart = new Cart(cartRepository.findAll());
        return cart.getItems();
    }
    @Transactional
    public CartItem changeQuantity(Long rowId, int quantity){
        Cart cart = new Cart(cartRepository.findAll());
        CartItem cartItem = cart.changeQuantity(rowId,quantity);
        return cartRepository.update(cartItem);
    }
    @Transactional
    public void remove (Long rowId){
        Cart cart = new Cart(cartRepository.findAll());
        cart.remove(rowId);
        cartRepository.delete(rowId);
    }

    public CartPreview preview(String couponCode) {
        Cart cart = new Cart(cartRepository.findAll());
        return cart.preview(Coupon.from(couponCode));
    }


}
