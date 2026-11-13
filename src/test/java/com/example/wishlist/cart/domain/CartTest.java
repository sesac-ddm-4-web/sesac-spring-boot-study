package com.example.wishlist.cart.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.wishlist.cart.exception.CartItemNotFoundException;
import com.example.wishlist.cart.exception.CartRuleViolationException;
import com.example.wishlist.cart.exception.InvalidQuantityException;
import com.example.wishlist.item.domain.WishlistItem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

public class CartTest {
    private Cart cart;
    private WishlistItem manualBook;     // 직접 등록 — 재고 제한 없음
    private WishlistItem externalBook;   // 외부 상품 — 저장된 재고 3
    private WishlistItem soldOutBook;    // 외부 상품 — 재고 0

    @BeforeEach
    void setUp() {
        cart = new Cart();
        manualBook = new WishlistItem(1L, "데미안", 12000);
        externalBook = WishlistItem.imported(101L, "Essence Mascara", 9990, 3).withId(2L);
        soldOutBook = WishlistItem.imported(102L, "Sold Out", 5000, 0).withId(3L);
    }

    @Nested
    @DisplayName("담기")
    class Add {

        @Test
        @DisplayName("처음 담으면 이름·가격·재고를 복사한 새 줄이 생긴다")
        void snapshot() {
            CartItem line = cart.add(externalBook, 2);

            assertThat(line.getId()).isEqualTo(1L);
            assertThat(line.getWishlistItemId()).isEqualTo(2L);
            assertThat(line.getName()).isEqualTo("Essence Mascara");
            assertThat(line.getUnitPrice()).isEqualTo(9990L);
            assertThat(line.getStockLimit()).isEqualTo(3);
            assertThat(line.lineTotal()).isEqualTo(19980L);
        }

        @Test
        @DisplayName("같은 상품을 또 담으면 새 줄 없이 수량을 합산한다")
        void mergeSameProduct() {
            CartItem first = cart.add(manualBook, 2);
            CartItem merged = cart.add(manualBook, 3);

            assertThat(merged.getId()).isEqualTo(first.getId());
            assertThat(merged.getQuantity()).isEqualTo(5);
            assertThat(cart.getItems()).hasSize(1);
        }

        @Test
        @DisplayName("합산 후 5개를 넘으면 409이고 기존 수량은 그대로다")
        void mergeOverFive() {
            cart.add(manualBook, 4);

            assertThatThrownBy(() -> cart.add(manualBook, 2))
                .isInstanceOf(CartRuleViolationException.class);
            assertThat(cart.getItems().get(0).getQuantity()).isEqualTo(4);
        }

        @Test
        @DisplayName("수량은 1~5만 허용한다 (0·6은 400)")
        void quantityRange() {
            assertThatThrownBy(() -> cart.add(manualBook, 0)).isInstanceOf(InvalidQuantityException.class);
            assertThatThrownBy(() -> cart.add(manualBook, 6)).isInstanceOf(InvalidQuantityException.class);
            assertThat(cart.add(manualBook, 5).getQuantity()).isEqualTo(5);
        }

        @Test
        @DisplayName("직접 등록 상품은 재고 제한이 없다")
        void manualHasNoStockLimit() {
            CartItem line = cart.add(manualBook, 5);

            assertThat(line.getStockLimit()).isNull();
            assertThat(line.getQuantity()).isEqualTo(5);
        }

        @Test
        @DisplayName("외부 상품은 저장된 재고 이하만 담을 수 있다 (합산 포함)")
        void externalStockLimit() {
            assertThatThrownBy(() -> cart.add(externalBook, 4))
                .isInstanceOf(CartRuleViolationException.class);
            assertThat(cart.isEmpty()).isTrue();

            cart.add(externalBook, 2);
            assertThatThrownBy(() -> cart.add(externalBook, 2))
                .isInstanceOf(CartRuleViolationException.class);
            assertThat(cart.add(externalBook, 1).getQuantity()).isEqualTo(3);
        }

        @Test
        @DisplayName("품절 외부 상품은 담을 수 없다")
        void soldOut() {
            assertThatThrownBy(() -> cart.add(soldOutBook, 1))
                .isInstanceOf(CartRuleViolationException.class);
            assertThat(cart.isEmpty()).isTrue();
        }

        @Test
        @DisplayName("상품 종류는 최대 20개, 21번째 종류는 409 (이미 담긴 상품 합산은 가능)")
        void maxKinds() {
            for (long id = 1; id <= 20; id++) {
                cart.add(new WishlistItem(id, "책" + id, 1000), 1);
            }

            assertThatThrownBy(() -> cart.add(new WishlistItem(21L, "책21", 1000), 1))
                .isInstanceOf(CartRuleViolationException.class);
            assertThat(cart.getItems()).hasSize(20);
            assertThat(cart.add(new WishlistItem(1L, "책1", 1000), 1).getQuantity()).isEqualTo(2);
        }
    }

    @Nested
    @DisplayName("수량 변경·삭제")
    class ChangeAndRemove {

        @Test
        @DisplayName("행 id로 수량을 바꾸고, 복사해 둔 재고를 기준으로 검사한다")
        void changeQuantity() {
            CartItem line = cart.add(externalBook, 1);

            assertThat(cart.changeQuantity(line.getId(), 3).getQuantity()).isEqualTo(3);
            assertThatThrownBy(() -> cart.changeQuantity(line.getId(), 4))
                .isInstanceOf(CartRuleViolationException.class);
            assertThatThrownBy(() -> cart.changeQuantity(line.getId(), 0))
                .isInstanceOf(InvalidQuantityException.class);
            assertThat(cart.getItem(line.getId()).getQuantity()).isEqualTo(3);
        }

        @Test
        @DisplayName("없는 행 id는 404")
        void unknownLine() {
            assertThatThrownBy(() -> cart.changeQuantity(99L, 1)).isInstanceOf(CartItemNotFoundException.class);
            assertThatThrownBy(() -> cart.remove(99L)).isInstanceOf(CartItemNotFoundException.class);
        }

        @Test
        @DisplayName("행을 삭제하면 목록에서 빠지고, 다시 담으면 새 행 id가 붙는다")
        void removeAndAddAgain() {
            CartItem line = cart.add(manualBook, 1);
            cart.remove(line.getId());

            assertThat(cart.isEmpty()).isTrue();
            assertThat(cart.add(manualBook, 1).getId()).isNotEqualTo(line.getId());
        }
    }

    @Test
    @DisplayName("상품 합계는 각 줄의 단가 × 수량의 합이다 (long)")
    void subtotal() {
        cart.add(manualBook, 2);      // 24,000
        cart.add(externalBook, 3);    // 29,970

        assertThat(cart.subtotal()).isEqualTo(53_970L);
        assertThat(cart.totalQuantity()).isEqualTo(5);
    }

    @Test
    @DisplayName("copy()로 만든 장바구니를 바꿔도 원본은 그대로다")
    void copyIsIndependent() {
        cart.add(manualBook, 1);
        Cart copy = cart.copy();

        copy.add(manualBook, 1);
        copy.add(externalBook, 1);

        assertThat(cart.getItems()).hasSize(1);
        assertThat(cart.getItems().get(0).getQuantity()).isEqualTo(1);
    }
}
