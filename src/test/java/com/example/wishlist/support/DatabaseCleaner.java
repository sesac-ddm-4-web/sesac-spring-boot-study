package com.example.wishlist.support;

import org.springframework.jdbc.core.JdbcTemplate;

public final class DatabaseCleaner {

    private DatabaseCleaner() {
    }

    public static void clear(JdbcTemplate jdbcTemplate) {
        jdbcTemplate.execute("TRUNCATE TABLE cart_item");          // 장바구니 행 삭제
        jdbcTemplate.update("UPDATE cart SET last_item_id = 0");   // 장바구니 행 id를 1부터 다시
        jdbcTemplate.execute("TRUNCATE TABLE wishlist_item");      // 위시리스트 삭제 + id 1부터 다시
    }
}