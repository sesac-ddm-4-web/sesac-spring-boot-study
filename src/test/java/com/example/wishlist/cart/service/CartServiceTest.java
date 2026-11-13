package com.example.wishlist.cart.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.wishlist.cart.domain.CartItem;
import com.example.wishlist.cart.exception.CartRuleViolationException;
import com.example.wishlist.cart.exception.InvalidQuantityException;
import com.example.wishlist.coupon.domain.Coupon;
import com.example.wishlist.coupon.exception.InvalidCouponException;
import com.example.wishlist.item.domain.WishlistItem;
import com.example.wishlist.item.exception.ItemNotFoundException;
import com.example.wishlist.item.service.WishlistService;
import com.example.wishlist.support.DatabaseCleaner;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

@SpringBootTest
class CartServiceTest {

    @Autowired
    private WishlistService wishlistService;

    @Autowired
    private CartService cartService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        DatabaseCleaner.clear(jdbcTemplate);
    }

    @Test
    @DisplayName("위시리스트에 원본이 없는 id는 담을 수 없다 (404)")
    void originalRequired() {
        assertThatThrownBy(() -> cartService.add(999L, 1))
            .isInstanceOf(ItemNotFoundException.class);
        assertThat(cartService.getCart().isEmpty()).isTrue();
    }

    @Test
    @DisplayName("새 줄이 생기면 newLine=true, 같은 상품이라 합산되면 newLine=false")
    void newLineFlag() {
        WishlistItem book = wishlistService.register("데미안", 12000);

        CartAddResult first = cartService.add(book.getId(), 1);
        CartAddResult merged = cartService.add(book.getId(), 1);

        assertThat(first.newLine()).isTrue();
        assertThat(merged.newLine()).isFalse();
        assertThat(merged.item().getId()).isEqualTo(first.item().getId());
        assertThat(merged.item().getQuantity()).isEqualTo(2);
    }

    @Test
    @DisplayName("실패한 추가·변경은 장바구니의 항목·수량·복사한 값을 그대로 둔다")
    void failureKeepsCart() {
        WishlistItem book = wishlistService.register("데미안", 12000);
        CartItem line = cartService.add(book.getId(), 4).item();
        List<CartItem> before = cartService.getCart().getItems();

        assertThatThrownBy(() -> cartService.add(book.getId(), 2)).isInstanceOf(CartRuleViolationException.class);
        assertThatThrownBy(() -> cartService.add(book.getId(), 9)).isInstanceOf(InvalidQuantityException.class);
        assertThatThrownBy(() -> cartService.changeQuantity(line.getId(), 6)).isInstanceOf(InvalidQuantityException.class);

        assertThat(cartService.getCart().getItems()).usingRecursiveFieldByFieldElementComparator()
            .containsExactlyElementsOf(before);
    }

    @Test
    @DisplayName("원본을 수정·삭제해도 장바구니 값은 유지되고, 행 id로 수량 변경·삭제할 수 있다")
    void originalChangedOrDeleted() {
        WishlistItem book = wishlistService.register("데미안", 12000);
        CartItem line = cartService.add(book.getId(), 1).item();

        wishlistService.update(book.getId(), "데미안 (개정판)", 15000);
        CartItem afterUpdate = cartService.getCart().getItem(line.getId());
        assertThat(afterUpdate.getName()).isEqualTo("데미안");
        assertThat(afterUpdate.getUnitPrice()).isEqualTo(12000L);

        wishlistService.delete(book.getId());
        assertThat(cartService.changeQuantity(line.getId(), 3).getQuantity()).isEqualTo(3);
        cartService.remove(line.getId());
        assertThat(cartService.getCart().isEmpty()).isTrue();
    }

    @Test
    @DisplayName("같은 상품 추가는 처음 복사한 단가를 쓰고, 삭제 후 다시 담으면 현재 값을 새로 복사한다")
    void snapshotRules() {
        WishlistItem book = wishlistService.register("데미안", 12000);
        CartItem line = cartService.add(book.getId(), 1).item();
        wishlistService.update(book.getId(), "데미안", 20000);

        CartItem merged = cartService.add(book.getId(), 1).item();
        assertThat(merged.getUnitPrice()).isEqualTo(12000L);

        cartService.remove(line.getId());
        CartItem fresh = cartService.add(book.getId(), 1).item();
        assertThat(fresh.getUnitPrice()).isEqualTo(20000L);
    }

    @Test
    @DisplayName("금액 미리보기와 최적 쿠폰 조회는 장바구니를 바꾸지 않는다")
    void previewDoesNotChangeCart() {
        WishlistItem book = wishlistService.register("데미안", 55000);
        cartService.add(book.getId(), 2);   // 110,000
        List<CartItem> before = cartService.getCart().getItems();

        assertThat(cartService.preview("PERCENT10").finalAmount()).isEqualTo(104_000L);
        assertThat(cartService.findBestCoupon().best().coupon()).isEqualTo(Coupon.FIXED15000);
        assertThatThrownBy(() -> cartService.preview("UNKNOWN")).isInstanceOf(InvalidCouponException.class);

        assertThat(cartService.getCart().getItems()).usingRecursiveFieldByFieldElementComparator()
            .containsExactlyElementsOf(before);
    }

    @Test
    @DisplayName("빈 장바구니는 모든 금액이 0이고 NONE만 사용할 수 있다")
    void emptyCart() {
        var preview = cartService.preview(null);

        assertThat(preview.subtotal()).isZero();
        assertThat(preview.shipping()).isZero();
        assertThat(preview.finalAmount()).isZero();
        assertThat(preview.coupon()).isEqualTo(Coupon.NONE);
        assertThatThrownBy(() -> cartService.preview("PERCENT10")).isInstanceOf(InvalidCouponException.class);
    }
}