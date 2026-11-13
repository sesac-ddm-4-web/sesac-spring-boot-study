package com.example.hobby_wishlist.infrastructure.persistence.database;

import com.example.hobby_wishlist.domain.cart.Cart;
import com.example.hobby_wishlist.domain.cart.CartItem;
import com.example.hobby_wishlist.domain.cart.CartRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.DataClassRowMapper;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Repository
@Profile("prod")
public class DatabaseCartRepository implements CartRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public DatabaseCartRepository(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Cart getCart() {
        List<CartItem> items = jdbcTemplate.query(
                """
                        SELECT * FROM cart_items""",
                new DataClassRowMapper<>(CartItem.class)
        );
        return new Cart(items);
    }

    @Override
    @Transactional
    public Cart saveChanges(Cart cart) {
        List<CartItem> items = cart.getItems();

        Map<Boolean, List<CartItem>> group = items.stream()
                .collect(Collectors.groupingBy(item -> item.getId() == null));
        List<CartItem> addedItems = group.getOrDefault(true, List.of());
        List<CartItem> keptItems = group.getOrDefault(false, List.of());

        // 새 장바구니에서 빠진 기존 장바구니에서 항목 제거
        // 새 장바구니에서 유지된 항목에서 없는 항목을 기존 장바구니에서 찾아 제거함
        List<Long> keptIds = keptItems.stream()
                .map(CartItem::getId)
                .toList();
        if (keptIds.isEmpty()) {
            jdbcTemplate.update(
                    """
                            DELETE FROM cart_items
                            """,
                    Map.of()
            );
        } else {
            jdbcTemplate.update(
                    """
                            DELETE FROM cart_items
                            WHERE id NOT IN (:ids)
                            """,
                    new MapSqlParameterSource("ids", keptIds)
            );
        }

        // 기존 장바구니의 항목들의 수량을 모두 업데이트
        for (CartItem keptItem : keptItems) {
            jdbcTemplate.update(
                    """
                            UPDATE cart_items
                            SET quantity = :quantity
                            WHERE id = :id
                            """,
                    new BeanPropertySqlParameterSource(keptItem)
            );
        }

        // 새 장바구니에서 추가된(id가 아직 없는) 항목 추가
        for (CartItem addedItem : addedItems) {
            KeyHolder keyHolder = new GeneratedKeyHolder();
            jdbcTemplate.update(
                    """
                            INSERT INTO cart_items 
                                (wishlist_item_id, name, price, stock, quantity)
                            VALUES (:wishlistItemId, :name, :price, :stock, :quantity)
                            """,
                    new BeanPropertySqlParameterSource(addedItem),
                    keyHolder
            );
            Long generatedId = keyHolder.getKey().longValue();
            addedItem.setId(generatedId);
        }

        return cart;
    }
}
