package com.example.demo.cart.service;

import com.example.demo.cart.dto.req.CartAddReq;
import com.example.demo.cart.dto.req.CartQuantityReq;
import com.example.demo.cart.dto.res.CartItemRes;
import com.example.demo.cart.dto.res.CartRes;
import com.example.demo.cart.dto.res.PricePreviewRes;
import com.example.demo.cart.repository.MemoryCartRepository;
import com.example.demo.global.exception.BadRequestException;
import com.example.demo.global.exception.ConflictException;
import com.example.demo.global.exception.EntityNotFoundException;
import com.example.demo.item.domain.CollectionItem;
import com.example.demo.item.repository.ListCollectionItemRepository;
import com.example.demo.item.service.ValidationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CartServiceTest {

    private ListCollectionItemRepository itemRepository;
    private CartService cartService;

    @BeforeEach
    void setUp() {
        itemRepository = new ListCollectionItemRepository();
        cartService = new CartService(new MemoryCartRepository(), itemRepository, new ValidationService());
    }

    private Long saveManual(String name, int price) {
        return itemRepository.add(new CollectionItem(null, name, price)).getId();
    }

    @Test
    void 위시리스트에_없는_상품을_담으면_404_예외가_발생한다() {
        assertThrows(EntityNotFoundException.class, () -> cartService.add(new CartAddReq(99L, 1)));
        assertTrue(cartService.find().items().isEmpty());
    }

    @Test
    void 원본을_수정해도_장바구니에_복사한_이름과_가격은_그대로다() {
        Long itemId = saveManual("코트", 110000);
        cartService.add(new CartAddReq(itemId, 1));

        itemRepository.update(new CollectionItem(itemId, "롱코트", 50000));

        CartItemRes line = cartService.find().items().get(0);
        assertEquals("코트", line.name());
        assertEquals(110000, line.unitPrice());
    }

    @Test
    void 원본이_삭제돼도_장바구니_행의_수량을_변경하고_삭제할_수_있다() {
        Long itemId = saveManual("코트", 110000);
        Long cartItemId = cartService.add(new CartAddReq(itemId, 1)).items().get(0).id();
        itemRepository.delete(itemId);

        CartRes changed = cartService.changeQuantity(cartItemId, new CartQuantityReq(3));
        assertEquals(3, changed.items().get(0).quantity());
        assertEquals(330000L, changed.subtotal());

        cartService.remove(cartItemId);
        assertTrue(cartService.find().items().isEmpty());
    }

    @Test
    void 원본이_삭제된_뒤에는_같은_상품을_다시_담을_수_없다() {
        Long itemId = saveManual("코트", 110000);
        Long cartItemId = cartService.add(new CartAddReq(itemId, 1)).items().get(0).id();
        itemRepository.delete(itemId);
        cartService.remove(cartItemId);

        assertThrows(EntityNotFoundException.class, () -> cartService.add(new CartAddReq(itemId, 1)));
    }

    @Test
    void 장바구니_행을_삭제한_뒤_다시_담으면_수정된_위시리스트_값을_복사한다() {
        Long itemId = saveManual("셔츠", 30000);
        Long cartItemId = cartService.add(new CartAddReq(itemId, 1)).items().get(0).id();
        itemRepository.update(new CollectionItem(itemId, "새 셔츠", 40000));
        cartService.remove(cartItemId);

        CartItemRes line = cartService.add(new CartAddReq(itemId, 1)).items().get(0);

        assertEquals("새 셔츠", line.name());
        assertEquals(40000, line.unitPrice());
    }

    @Test
    void 실패한_담기와_수량_변경은_기존_장바구니를_바꾸지_않는다() {
        Long itemId = saveManual("코트", 110000);
        Long cartItemId = cartService.add(new CartAddReq(itemId, 4)).items().get(0).id();

        assertThrows(ConflictException.class, () -> cartService.add(new CartAddReq(itemId, 2)));
        assertThrows(EntityNotFoundException.class, () -> cartService.changeQuantity(99L, new CartQuantityReq(1)));

        CartRes cart = cartService.find();
        assertEquals(1, cart.items().size());
        assertEquals(cartItemId, cart.items().get(0).id());
        assertEquals(4, cart.items().get(0).quantity());
    }

    @Test
    void 미리보기는_쿠폰별_금액을_계산하고_장바구니를_바꾸지_않는다() {
        Long itemId = saveManual("코트", 110000);
        cartService.add(new CartAddReq(itemId, 1));
        CartRes before = cartService.find();

        PricePreviewRes none = cartService.preview(null);
        PricePreviewRes percent = cartService.preview("PERCENT10");
        PricePreviewRes fixed = cartService.preview("FIXED15000");

        assertEquals(110000L, none.finalAmount());
        assertEquals(104000L, percent.finalAmount());
        assertEquals(100000L, fixed.finalAmount());
        assertEquals(before, cartService.find());
    }

    @Test
    void 사용할_수_없는_쿠폰으로_미리보기하면_400_예외가_발생하고_장바구니는_그대로다() {
        Long itemId = saveManual("셔츠", 30000);
        cartService.add(new CartAddReq(itemId, 1));
        CartRes before = cartService.find();

        assertThrows(BadRequestException.class, () -> cartService.preview("FIXED15000"));
        assertThrows(BadRequestException.class, () -> cartService.preview("ABC"));
        assertEquals(before, cartService.find());
    }
}
