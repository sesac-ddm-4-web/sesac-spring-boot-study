package com.example.personal_project.application;

import com.example.personal_project.domain.cart.model.Cart;
import com.example.personal_project.domain.cart.model.CartPreview;
import com.example.personal_project.domain.cart.model.Coupon;
import com.example.personal_project.domain.cart.model.CartAmountCalculator;
import com.example.personal_project.domain.cart.model.CartItem;
import com.example.personal_project.domain.cart.repository.CartRepository;
import com.example.personal_project.domain.item.model.CollectionItem;
import com.example.personal_project.domain.item.repository.ItemRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CartService {

    private final ItemRepository itemRepository;
    private final CartRepository cartRepository;
    private final CartAmountCalculator amountCalculator;

    public CartService(
            ItemRepository itemRepository,
            CartRepository cartRepository,
            CartAmountCalculator amountCalculator
    ) {
        this.itemRepository = itemRepository;
        this.cartRepository = cartRepository;
        this.amountCalculator = amountCalculator;
    }

    public synchronized List<CartItem> add(Long itemId, Integer quantity) {
        validateId(itemId);
        validateQuantity(quantity);

        CollectionItem item = itemRepository.findById(itemId);
        Cart cart = cartRepository.find();

        cart.add(item, quantity);
        cartRepository.save(cart);

        return cart.findAll();
    }
    
    public List<CartItem> findAll() {
        return cartRepository.find().findAll();
    }

    public synchronized List<CartItem> changeQuantity(Long cartItemId, Integer quantity) {
        validateId(cartItemId);
        validateQuantity(quantity);
        Cart cart = cartRepository.find();
        cart.changeQuantity(cartItemId, quantity);
        cartRepository.save(cart);
        return cart.findAll();
    }

    public synchronized void remove(Long cartItemId) {
        validateId(cartItemId);
        Cart cart = cartRepository.find();
        cart.remove(cartItemId);
        cartRepository.save(cart);
    }

    public synchronized CartPreview preview(String couponCode) {
        Coupon coupon = Coupon.from(couponCode);
        return amountCalculator.calculate(cartRepository.find().findAll(), coupon);
    }

    public synchronized CartPreview bestCoupon() {
        return amountCalculator.bestCoupon(cartRepository.find().findAll());
    }

    private void validateId(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("id는 양수여야 합니다.");
        }
    }

    private void validateQuantity(Integer quantity) {
        if (quantity == null || quantity < 1 || quantity > 5) {
            throw new IllegalArgumentException("수량은 1~5개여야 합니다.");
        }
    }
}
