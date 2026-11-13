package com.example.wishlist.item.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.wishlist.item.domain.WishlistItem;
import com.example.wishlist.item.exception.InvalidBudgetException;
import com.example.wishlist.item.exception.InvalidItemException;
import com.example.wishlist.item.exception.ItemNotFoundException;
import com.example.wishlist.support.DatabaseCleaner;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

@SpringBootTest
public class WishlistServiceTest {

    @Autowired
    private WishlistService service;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        DatabaseCleaner.clear(jdbcTemplate);
    }

    @Nested
    @DisplayName("정상 등록/조회")
    class Register {

        @Test
        @DisplayName("등록하면 id가 부여되고, 그 실제 id로 조회할 수 있다")
        void registerAndFindById() {
            WishlistItem saved = service.register("지구 끝의 온실", 18000);

            WishlistItem found = service.findById(saved.getId());

            assertThat(saved.getId()).isNotNull();
            assertThat(found.getName()).isEqualTo("지구 끝의 온실");
            assertThat(found.getPrice()).isEqualTo(18000);
        }

        @Test
        @DisplayName("전체 목록은 등록된 순서대로 조회된다")
        void findAllInRegistrationOrder() {
            service.register("지구 끝의 온실", 18000);
            service.register("프로젝트 헤일메리", 30000);

            List<WishlistItem> items = service.findAll();

            assertThat(items).extracting(WishlistItem::getName)
                .containsExactly("지구 끝의 온실", "프로젝트 헤일메리");
        }

        @Test
        @DisplayName("이름 앞뒤 공백은 없애고 저장된다")
        void registerExceptBlank() {
            WishlistItem saved = service.register(" 지구 끝의 온실 ", 18000);

            assertThat(saved.getName()).isEqualTo("지구 끝의 온실");
        }

        @Test
        @DisplayName("0원 책 저장 가능하다")
        void registerFreeItem() {
            WishlistItem saved = service.register("Free Book", 0);

            assertThat(saved.getPrice()).isZero();
        }
    }

    @Nested
    @DisplayName("실패 처리")
    class Failure {

        @Test
        @DisplayName("null, 빈 문자열, 공백만 있는 이름은 저장할 수 없다")
        void registerNameFailure() {
            assertThatThrownBy(() -> service.register(null, 10000))
                .isInstanceOf(InvalidItemException.class);
            assertThatThrownBy(() -> service.register("", 10000))
                .isInstanceOf(InvalidItemException.class);
            assertThatThrownBy(() -> service.register(" ", 10000))
                .isInstanceOf(InvalidItemException.class);
        }

        @Test
        @DisplayName("음수, null 가격은 저장할 수 없다")
        void registerPriceFailure() {
            assertThatThrownBy(() -> service.register("test", -1))
                .isInstanceOf(InvalidItemException.class);
            assertThatThrownBy(() -> service.register("test", null))
                .isInstanceOf(InvalidItemException.class);
        }

        @Test
        @DisplayName("음수 예산은 불가능하다")
        void negativeBudgetFailure() {
            assertThatThrownBy(() -> service.recommend(-1))
                .isInstanceOf(InvalidBudgetException.class);
        }

        @Test
        @DisplayName("실패한 등록 뒤에도 기존 목록은 바뀌지 않는다")
        void failedRegisterKeepsList() {
            service.register("지구 끝의 온실", 18000);
            List<WishlistItem> before = service.findAll();

            assertThatThrownBy(() -> service.register("", 1000))
                .isInstanceOf(InvalidItemException.class);
            assertThatThrownBy(() -> service.register("test", -1000))
                .isInstanceOf(InvalidItemException.class);
            // DB는 조회할 때마다 새 객체를 만들므로, 같은 객체인지가 아니라 "필드 값"이 같은지 비교
            assertThat(service.findAll()).usingRecursiveFieldByFieldElementComparator()
                .containsExactlyElementsOf(before);
        }

        @Test
        @DisplayName("없는 id는 조회 불가능하다")
        void notFoundId() {
            assertThatThrownBy(() -> service.findById(999L))
                .isInstanceOf(ItemNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("추천")
    class Recommend {

        @BeforeEach
        void registerExample() {
            service.register("데미안", 12000);
            service.register("무료 전자책 샘플", 0);
            service.register("어린 왕자", 5000);
            service.register("노인과 바다", 5000);
            service.register("동물농장", 8000);
        }

        @Test
        @DisplayName("예산 5000원 : 무료 전자책 샘플, 어린 왕자, 노인과 바다 (경계값 포함, 가격순, 같은 가격은 등록순)")
        void budgetBoundaryAndOrder() {
            List<WishlistItem> result = service.recommend(5000);

            assertThat(result).extracting(WishlistItem::getName)
                .containsExactly("무료 전자책 샘플", "어린 왕자", "노인과 바다");
        }

        @Test
        @DisplayName("예산 0원 : 무료 전자책 샘플")
        void budgetZero() {
            assertThat(service.recommend(0)).extracting(WishlistItem::getName)
                .containsExactly("무료 전자책 샘플");
        }

        @Test
        @DisplayName("조건에 맞는 아이템이 4개 이상이어도 최대 3개만 추천 가능하다")
        void maxThree() {
            List<WishlistItem> result = service.recommend(100000);

            assertThat(result).hasSize(3);
            assertThat(result).extracting(WishlistItem::getName)
                .containsExactly("무료 전자책 샘플", "어린 왕자", "노인과 바다");
        }

        @Test
        @DisplayName("4999원이면 5000원은 추천 불가")
        void boundaryTest() {
            assertThat(service.recommend(4999)).extracting(WishlistItem::getName)
                .containsExactly("무료 전자책 샘플");
        }

        @Test
        @DisplayName("추천 조회 전후로 원본 목록의 순서와 내용은 그대로여야 한다")
        void originalList() {
            List<WishlistItem> before = service.findAll();
            service.recommend(5000);

            // DB는 조회할 때마다 새 객체를 만들므로, 같은 객체인지가 아니라 "필드 값"이 같은지 비교
            assertThat(service.findAll()).usingRecursiveFieldByFieldElementComparator()
                .containsExactlyElementsOf(before);
            assertThat(service.findAll()).extracting(WishlistItem::getName)
                .containsExactly("데미안", "무료 전자책 샘플", "어린 왕자", "노인과 바다", "동물농장");
        }
    }


    @Test
    @DisplayName("조건에 맞는 아이템이 없으면 빈 목록을 반환한다")
    void emptyResult() {
        service.register("데미안", 12000);
        assertThat(service.recommend(1000)).isEmpty();
    }
}