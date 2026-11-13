package com.example.wishlist.product.dto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.wishlist.common.exception.InvalidRequestException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class ProductQueryTest {

    @Test
    @DisplayName("limit·skip을 생략하면 기본값 10·0을 쓴다")
    void defaults() {
        ProductQuery query = ProductQuery.forList(null, null);

        assertThat(query.limit()).isEqualTo(10);
        assertThat(query.skip()).isZero();
    }

    @Test
    @DisplayName("limit은 1~20만 허용한다 (경계값 1·20 통과, 0·21 거부)")
    void limitRange() {
        assertThat(ProductQuery.forList(1, 0).limit()).isEqualTo(1);
        assertThat(ProductQuery.forList(20, 0).limit()).isEqualTo(20);

        assertThatThrownBy(() -> ProductQuery.forList(0, 0)).isInstanceOf(InvalidRequestException.class);
        assertThatThrownBy(() -> ProductQuery.forList(21, 0)).isInstanceOf(InvalidRequestException.class);
    }

    @Test
    @DisplayName("skip은 0 이상만 허용한다")
    void skipRange() {
        assertThat(ProductQuery.forList(10, 0).skip()).isZero();
        assertThatThrownBy(() -> ProductQuery.forList(10, -1)).isInstanceOf(InvalidRequestException.class);
    }

    @Test
    @DisplayName("검색어는 누락·빈 값·공백이면 거부하고, 앞뒤 공백을 제거한다")
    void keyword() {
        assertThatThrownBy(() -> ProductQuery.forSearch(null, null, null)).isInstanceOf(InvalidRequestException.class);
        assertThatThrownBy(() -> ProductQuery.forSearch("", null, null)).isInstanceOf(InvalidRequestException.class);
        assertThatThrownBy(() -> ProductQuery.forSearch("   ", null, null)).isInstanceOf(InvalidRequestException.class);

        assertThat(ProductQuery.forSearch("  phone ", null, null).keyword()).isEqualTo("phone");
    }
}
