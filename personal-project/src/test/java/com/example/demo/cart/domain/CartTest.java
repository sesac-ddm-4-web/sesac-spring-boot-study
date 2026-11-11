package com.example.demo.cart.domain;

import com.example.demo.global.exception.ConflictException;
import com.example.demo.global.exception.EntityNotFoundException;
import com.example.demo.item.domain.CollectionItem;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CartTest {

    private CollectionItem manual(long id, String name, int price) {
        return new CollectionItem(id, name, price);
    }

    private CollectionItem external(long id, String name, int price, int stock) {
        CollectionItem item = CollectionItem.ofExternal(id + 100, name, price, stock);
        item.setId(id);
        return item;
    }

    @Test
    void 상품을_담으면_이름_가격_재고를_복사한_새_줄이_생긴다() {
        Cart cart = new Cart();

        cart.add(external(1L, "Mascara", 9990, 3), 2);

        CartItem line = cart.getItems().get(0);
        assertEquals(1L, line.getId());
        assertEquals(1L, line.getItemId());
        assertEquals("Mascara", line.getName());
        assertEquals(9990, line.getUnitPrice());
        assertEquals(3, line.getStock());
        assertEquals(2, line.getQuantity());
        assertEquals(19980L, line.lineTotal());
    }

    @Test
    void 같은_상품을_다시_담으면_새_줄을_만들지_않고_수량을_합산한다() {
        Cart cart = new Cart();

        cart.add(manual(1L, "코트", 110000), 2);
        cart.add(manual(1L, "코트", 110000), 3);

        assertEquals(1, cart.getItems().size());
        assertEquals(5, cart.getItems().get(0).getQuantity());
    }

    @Test
    void 합산한_수량이_5개를_넘으면_409_예외가_발생하고_수량은_그대로다() {
        Cart cart = new Cart();
        cart.add(manual(1L, "코트", 110000), 3);

        assertThrows(ConflictException.class, () -> cart.add(manual(1L, "코트", 110000), 3));
        assertEquals(3, cart.getItems().get(0).getQuantity());
    }

    @Test
    void 외부_상품은_저장된_재고를_넘겨_담을_수_없다() {
        Cart cart = new Cart();

        assertThrows(ConflictException.class, () -> cart.add(external(1L, "Mascara", 9990, 2), 3));
        assertTrue(cart.getItems().isEmpty());
    }

    @Test
    void 외부_상품을_합산할_때도_처음_복사한_재고를_넘을_수_없다() {
        Cart cart = new Cart();
        cart.add(external(1L, "Mascara", 9990, 2), 2);

        assertThrows(ConflictException.class, () -> cart.add(external(1L, "Mascara", 9990, 10), 1));
        assertEquals(2, cart.getItems().get(0).getQuantity());
    }

    @Test
    void 품절된_외부_상품은_담을_수_없다() {
        Cart cart = new Cart();

        assertThrows(ConflictException.class, () -> cart.add(external(1L, "Motorcycle", 7499990, 0), 1));
        assertTrue(cart.getItems().isEmpty());
    }

    @Test
    void 수동_상품은_재고_제한_없이_5개까지_담을_수_있다() {
        Cart cart = new Cart();

        cart.add(manual(1L, "코트", 110000), 5);

        CartItem line = cart.getItems().get(0);
        assertNull(line.getStock());
        assertEquals(5, line.getQuantity());
    }

    @Test
    void 상품_종류가_20종을_넘으면_409_예외가_발생하고_기존_20종은_유지된다() {
        Cart cart = new Cart();
        for (long id = 1; id <= 20; id++) {
            cart.add(manual(id, "상품" + id, 1000), 1);
        }

        assertThrows(ConflictException.class, () -> cart.add(manual(21L, "상품21", 1000), 1));
        assertEquals(20, cart.getItems().size());
    }

    @Test
    void 이미_20종이어도_담긴_상품의_수량은_늘릴_수_있다() {
        Cart cart = new Cart();
        for (long id = 1; id <= 20; id++) {
            cart.add(manual(id, "상품" + id, 1000), 1);
        }

        cart.add(manual(1L, "상품1", 1000), 1);

        assertEquals(2, cart.getItems().get(0).getQuantity());
    }

    @Test
    void 같은_상품을_다시_담을_때는_처음_복사한_이름과_가격을_계속_사용한다() {
        Cart cart = new Cart();
        cart.add(manual(1L, "코트", 110000), 1);

        cart.add(manual(1L, "롱코트", 50000), 1);

        CartItem line = cart.getItems().get(0);
        assertEquals("코트", line.getName());
        assertEquals(110000, line.getUnitPrice());
        assertEquals(220000L, line.lineTotal());
    }

    @Test
    void 줄을_삭제한_뒤_다시_담으면_현재_값을_새로_복사한다() {
        Cart cart = new Cart();
        cart.add(manual(1L, "코트", 110000), 1);
        cart.remove(1L);

        cart.add(manual(1L, "롱코트", 50000), 1);

        CartItem line = cart.getItems().get(0);
        assertEquals(2L, line.getId());
        assertEquals("롱코트", line.getName());
        assertEquals(50000, line.getUnitPrice());
    }

    @Test
    void 수량_변경은_장바구니_행_id로_하고_재고를_넘으면_값이_유지된다() {
        Cart cart = new Cart();
        cart.add(external(1L, "Mascara", 9990, 3), 1);

        cart.changeQuantity(1L, 3);
        assertThrows(ConflictException.class, () -> cart.changeQuantity(1L, 4));

        assertEquals(3, cart.getItems().get(0).getQuantity());
    }

    @Test
    void 없는_장바구니_행을_변경하거나_삭제하면_404_예외가_발생한다() {
        Cart cart = new Cart();
        cart.add(manual(1L, "코트", 110000), 1);

        assertThrows(EntityNotFoundException.class, () -> cart.changeQuantity(99L, 1));
        assertThrows(EntityNotFoundException.class, () -> cart.remove(99L));
        assertEquals(1, cart.getItems().size());
    }

    @Test
    void 상품_합계는_long으로_계산되어_int_범위를_넘어도_정확하다() {
        Cart cart = new Cart();
        for (long id = 1; id <= 20; id++) {
            cart.add(manual(id, "고가품" + id, 100_000_000), 5);
        }

        assertEquals(10_000_000_000L, cart.subtotal());
    }

    @Test
    void 장바구니_목록은_밖에서_수정할_수_없다() {
        Cart cart = new Cart();
        cart.add(manual(1L, "코트", 110000), 1);

        List<CartItem> items = cart.getItems();

        assertThrows(UnsupportedOperationException.class, items::clear);
        assertEquals(1, cart.getItems().size());
    }
}
