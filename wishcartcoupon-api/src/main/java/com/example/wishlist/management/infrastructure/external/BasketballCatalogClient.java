package com.example.wishlist.management.infrastructure.external;

import com.example.wishlist.management.domain.exception.NotFoundException;
import com.example.wishlist.management.domain.external.ExternalProduct;
import com.example.wishlist.management.domain.external.ExternalProductClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;

/**
 * 농구용품 전용 "외부 상품" 목록.
 *
 * DummyJsonProductClient와 똑같이 ExternalProductClient를 구현하므로
 * 서비스·컨트롤러는 한 줄도 바꾸지 않고 설정(external.catalog=basketball)만으로 갈아 끼울 수 있다.
 * 가격은 DummyJSON처럼 "달러" 단위로 들고 있고, 원화 환산은 기존 ExternalPriceConverter가 한다.
 */
@Component
@ConditionalOnProperty(name = "external.catalog", havingValue = "basketball")
public class BasketballCatalogClient implements ExternalProductClient {

    private static final List<ExternalProduct> CATALOG = List.of(
            // 농구공
            item(1, "실내외 겸용 농구공 7호", "29.99", "농구공", 120),
            item(2, "프로 경기용 가죽 농구공", "79.00", "농구공", 35),
            item(3, "고무 야외 농구공 7호", "19.50", "농구공", 200),
            item(4, "여성용 농구공 6호", "27.90", "농구공", 60),
            item(5, "미니 농구공 (어린이)", "12.90", "농구공", 90),
            // 농구화
            item(6, "하이탑 농구화 - 블랙", "119.00", "농구화", 40),
            item(7, "로우탑 농구화 - 화이트", "99.00", "농구화", 3),
            item(8, "쿠셔닝 강화 농구화", "139.00", "농구화", 0),
            item(9, "주니어 농구화", "69.00", "농구화", 55),
            // 유니폼·의류
            item(10, "리버시블 농구 유니폼 세트", "45.00", "의류", 80),
            item(11, "메쉬 농구 저지 - 레드", "32.00", "의류", 70),
            item(12, "농구 반바지 - 네이비", "24.00", "의류", 100),
            item(13, "슈팅 셔츠 (긴팔)", "34.50", "의류", 45),
            // 보호대·액세서리
            item(14, "무릎 보호대", "18.00", "보호대", 150),
            item(15, "발목 보호대 2개입", "15.90", "보호대", 130),
            item(16, "손목 밴드·헤어밴드 세트", "9.90", "보호대", 300),
            item(17, "농구용 압박 슬리브", "14.00", "보호대", 85),
            // 가방·훈련용품
            item(18, "농구공 전용 백팩", "39.00", "가방", 50),
            item(19, "볼 가방 (3개 수납)", "22.00", "가방", 4),
            item(20, "드리블 훈련 고글", "16.50", "훈련용품", 60),
            item(21, "미니 후프 (문걸이형)", "29.00", "훈련용품", 25),
            item(22, "접이식 전술판", "13.50", "훈련용품", 75),
            item(23, "공기주입기 + 바늘 세트", "8.50", "훈련용품", 220),
            item(24, "이동식 농구 골대 (높이 조절)", "189.00", "골대", 8)
    );

    private static ExternalProduct item(long id, String title, String price, String category, int stock) {
        // 이미지는 외부 주소 없이 화면에서 카테고리별로 그리므로 thumbnail은 비워 둔다.
        return new ExternalProduct(id, title, new BigDecimal(price), category, null, stock);
    }

    @Override
    public List<ExternalProduct> list(int limit, int skip) {
        return page(CATALOG, limit, skip);
    }

    @Override
    public List<ExternalProduct> search(String q, int limit, int skip) {
        String keyword = q.trim().toLowerCase(Locale.ROOT);
        List<ExternalProduct> matched = CATALOG.stream()
                .filter(p -> p.title().toLowerCase(Locale.ROOT).contains(keyword)
                        || p.category().toLowerCase(Locale.ROOT).contains(keyword))
                .toList();
        // 검색으로 "먼저 거른 뒤" skip/limit을 적용한다 (DummyJSON 검색과 같은 규칙)
        return page(matched, limit, skip);
    }

    @Override
    public ExternalProduct get(long id) {
        return CATALOG.stream()
                .filter(p -> p.id() == id)
                .findFirst()
                .orElseThrow(() -> new NotFoundException("외부 상품을 찾을 수 없습니다. id=" + id));
    }

    private static List<ExternalProduct> page(List<ExternalProduct> source, int limit, int skip) {
        if (skip >= source.size()) return List.of();
        return source.subList(skip, Math.min(source.size(), skip + limit));
    }
}