package com.example.wishlist;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.wishlist.cart.controller.CartController;
import com.example.wishlist.cart.dto.BestCouponResponse;
import com.example.wishlist.cart.dto.CartAddRequest;
import com.example.wishlist.cart.dto.CartItemResponse;
import com.example.wishlist.cart.dto.CartResponse;
import com.example.wishlist.cart.dto.PricePreviewResponse;
import com.example.wishlist.item.controller.WishlistController;
import com.example.wishlist.item.domain.SourceType;
import com.example.wishlist.item.dto.ItemCreateRequest;
import com.example.wishlist.item.dto.ItemImportRequest;
import com.example.wishlist.item.dto.ItemResponse;
import com.example.wishlist.item.exception.DuplicateImportException;
import com.example.wishlist.product.client.ProductClient;
import com.example.wishlist.support.DatabaseCleaner;
import com.example.wishlist.support.FakeProductClient;
import java.net.URI;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * 통합 테스트: 외부 상품 가져오기 → 장바구니 → 쿠폰 미리보기까지 Spring이 연결한 빈으로 확인
 *
 * 실제 dummyjson.com 대신 FakeProductClient를 @Primary 빈으로 등록합니다.
 * Service들이 ProductClient "인터페이스"에 의존하기 때문에 가능한 방법입니다.
 */
@SpringBootTest
class ShoppingFlowIntegrationTest {

    @TestConfiguration
    static class FakeClientConfig {
        @Bean
        @Primary
        ProductClient fakeProductClient() {
            return new FakeProductClient()
                .add(1L, "Essence Mascara Lash Princess", "9.99", 3)
                .add(2L, "Sold Out Book", "5", 0);
        }
    }

    @Autowired
    WishlistController wishlistController;

    @Autowired
    CartController cartController;

    @Autowired
    JdbcTemplate jdbcTemplate;

    /** DB(위시리스트·장바구니)는 테스트가 끝나도 데이터가 남으므로, 매 테스트 전에 비웁니다. */
    @BeforeEach
    void clearTable() {
        DatabaseCleaner.clear(jdbcTemplate);
    }

    @Test
    @DisplayName("가져오기 → 직접 등록 → 장바구니 담기 → 쿠폰 미리보기 → 최적 쿠폰")
    void fullFlow() {
        ItemResponse imported = wishlistController.importProduct(new ItemImportRequest(1L)).getBody();
        ItemResponse manual = wishlistController.register(new ItemCreateRequest("데미안", 100_000)).getBody();

        assertThat(imported.sourceType()).isEqualTo(SourceType.EXTERNAL);
        assertThat(imported.price()).isEqualTo(9990);
        assertThat(manual.sourceType()).isEqualTo(SourceType.MANUAL);
        assertThatThrownBy(() -> wishlistController.importProduct(new ItemImportRequest(1L)))
            .isInstanceOf(DuplicateImportException.class);

        // 처음 담으면 201 + Location, 같은 상품을 또 담으면 합산이라 200
        ResponseEntity<CartItemResponse> first = cartController.add(new CartAddRequest(imported.id(), 1));
        ResponseEntity<CartItemResponse> merged = cartController.add(new CartAddRequest(imported.id(), 1));
        assertThat(first.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(first.getHeaders().getLocation()).isEqualTo(URI.create("/api/cart/items/" + first.getBody().id()));
        assertThat(merged.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(merged.getBody().id()).isEqualTo(first.getBody().id());
        assertThat(merged.getBody().quantity()).isEqualTo(2);       // 9,990 × 2 = 19,980

        cartController.add(new CartAddRequest(manual.id(), 1));     // 100,000
        CartResponse cart = cartController.getCart().getBody();
        assertThat(cart.kindCount()).isEqualTo(2);
        assertThat(cart.subtotal()).isEqualTo(119_980L);

        // PERCENT10: 11,998원 할인 → 107,982원(무료배송)
        PricePreviewResponse percent = cartController.preview("PERCENT10").getBody();
        assertThat(percent.discount()).isEqualTo(11_998L);
        assertThat(percent.shipping()).isZero();
        assertThat(percent.finalAmount()).isEqualTo(107_982L);

        // FIXED15000: 104,980원(무료배송)이 가장 작음
        BestCouponResponse best = cartController.bestCoupon().getBody();
        assertThat(best.best().coupon()).isEqualTo("FIXED15000");
        assertThat(best.best().finalAmount()).isEqualTo(104_980L);

        // 미리보기는 장바구니를 바꾸지 않음
        assertThat(cartController.getCart().getBody()).isEqualTo(cart);
    }

    @Test
    @DisplayName("품절 상품은 위시리스트에 가져올 수 있지만 장바구니에는 담을 수 없다")
    void soldOutFlow() {
        ItemResponse soldOut = wishlistController.importProduct(new ItemImportRequest(2L)).getBody();

        assertThat(soldOut.soldOut()).isTrue();
        assertThatThrownBy(() -> cartController.add(new CartAddRequest(soldOut.id(), 1)))
            .hasMessageContaining("품절");
        assertThat(cartController.getCart().getBody().items()).isEmpty();
    }
}