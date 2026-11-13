package com.example.wishlist.management.infrastructure;

import com.example.wishlist.management.domain.wish.Wish;
import com.example.wishlist.management.infrastructure.wish.ListWishRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ListWishRepositoryTest {

    private ListWishRepository repository;

    @BeforeEach
    void setUp() {
        repository = new ListWishRepository();
    }

    private List<String> names(List<Wish> wishes) {
        return wishes.stream().map(Wish::getName).toList();
    }

    // 1. 정상 등록·목록·실제 id의 단건 조회
    @Test
    void addAndFindWish() {
        Wish saved = repository.add(new Wish(0L, "커피", 4500));

        assertThat(saved.getId()).isEqualTo(1L);
        assertThat(repository.getWishList()).hasSize(1);
        assertThat(repository.getWish(1L).getName()).isEqualTo("커피");
    }

    // 2. 빈 이름·음수 가격·음수 예산 거부
    @Test
    void rejectInvalidInput() {
        assertThrows(IllegalArgumentException.class, () -> new Wish(0L, "   ", 1000));
        assertThrows(IllegalArgumentException.class, () -> new Wish(0L, "커피", -1));
        assertThrows(ResponseStatusException.class, () -> repository.getRecommendWishList(-1));
    }

    // 3. 예산 경계·가격 정렬·같은 가격의 순서·최대 3개
    @Test
    void sortAndRecommend() {
        repository.add(new Wish(0L, "노트", 3000));
        repository.add(new Wish(0L, "볼펜", 1000));
        repository.add(new Wish(0L, "스티커", 3000));   // 노트와 같은 가격, 나중에 등록
        repository.add(new Wish(0L, "커피", 4500));

        // 가격 오름차순 + 같은 가격은 등록 순서
        assertThat(names(repository.getWishList()))
                .containsExactly("볼펜", "노트", "스티커", "커피");

        // 예산 경계: 3000원 이하
        assertThat(names(repository.getRecommendWishList(3000)))
                .containsExactly("볼펜", "노트", "스티커");

        // 4개 다 살 수 있어도 최대 3개
        assertThat(repository.getRecommendWishList(10000)).hasSize(3);
    }

    // 4. 빈 결과·가격 0·조회 전후 원본 보존
    @Test
    void emptyResultZeroPriceAndOriginalPreserved() {
        assertThat(repository.getRecommendWishList(1000)).isEmpty();

        repository.add(new Wish(0L, "무료쿠폰", 0));
        assertThat(names(repository.getRecommendWishList(0))).containsExactly("무료쿠폰");

        repository.getRecommendWishList(1000);
        assertThat(repository.getWishList()).hasSize(1);
    }
}