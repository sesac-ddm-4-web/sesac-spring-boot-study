package com.example.wishlist.management.application.cart;

import com.example.wishlist.management.domain.cart.CartItem;
import com.example.wishlist.management.domain.wish.Wish;
import com.example.wishlist.management.domain.wish.WishRepository;
import com.example.wishlist.management.infrastructure.cart.InMemoryCartRepository;
import com.example.wishlist.management.infrastructure.wish.ListWishRepository;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CartServiceTest {
    WishRepository wishRepository = new ListWishRepository();
    CartService service = new CartService(new InMemoryCartRepository(), wishRepository);

    @Test void copiesValuesAtAddTime() {
        Wish wish = wishRepository.add(new Wish(0L, "키보드", 50_000));
        service.addItem(wish.getId(), 1);

        wishRepository.updateWish(new Wish(wish.getId(), "마우스", 99_000));   // 원본 변경

        CartItem inCart = service.getItems().get(0);
        assertEquals("키보드", inCart.getName());     // 장바구니는 그대로
        assertEquals(50_000, inCart.getPrice());
    }

    @Test void quantityChangeWorksAfterWishDeleted() {
        Wish wish = wishRepository.add(new Wish(0L, "키보드", 50_000));
        CartItem item = service.addItem(wish.getId(), 1);

        wishRepository.deleteWish(wish.getId());                            // 원본 삭제

        assertEquals(3, service.changeQuantity(item.getId(), 3).getQuantity());
    }

    @Test
    void readdAfterRemoveCopiesCurrentValues() {
        Wish wish = wishRepository.add(new Wish(0L, "키보드", 50_000));
        CartItem first = service.addItem(wish.getId(), 1);
        service.removeItem(first.getId());

        wishRepository.updateWish(new Wish(wish.getId(), "키보드2", 60_000));

        CartItem second = service.addItem(wish.getId(), 1);                  // 다시 담기
        assertEquals("키보드2", second.getName());                           // 그 시점의 값
        assertEquals(60_000, second.getPrice());
    }
}