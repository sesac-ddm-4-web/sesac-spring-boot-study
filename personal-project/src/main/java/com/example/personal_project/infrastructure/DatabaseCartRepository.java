package com.example.personal_project.infrastructure;

import com.example.personal_project.domain.Cart;
import com.example.personal_project.domain.CartItem;
import com.example.personal_project.domain.CartRepository;
import com.example.personal_project.domain.SourceType;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
@Profile("prod")
public class DatabaseCartRepository implements CartRepository {

    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    public DatabaseCartRepository(
            NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
    }

    @Override
    public Cart getCart() {
        String stateSql = """
            SELECT next_sequence
            FROM cart_state
            WHERE id = :id
            """;

        MapSqlParameterSource parameters =
                new MapSqlParameterSource("id", 1L);

        Long nextSequence = namedParameterJdbcTemplate.queryForObject(
                stateSql,
                parameters,
                Long.class
        );

        String itemsSql = """
            SELECT id, item_id, name, unit_price,
                   source_type, stock, quantity
            FROM cart_items
            ORDER BY id ASC
            """;

        List<CartItem> items = namedParameterJdbcTemplate.query(
                itemsSql,
                cartItemRowMapper
        );

        Cart cart = new Cart(items, nextSequence);
        return cart;
    }

    @Override
    @Transactional
    public void save(Cart cart) {
        Cart storedCart = getCart();
        List<CartItem> storedItems = storedCart.getItems();
        List<CartItem> currentItems = cart.getItems();

        // 현재 Cart에서 삭제한 항목만 DB에서도 삭제
        for (CartItem storedItem : storedItems) {
            if (findItemInList(currentItems, storedItem.getId()) == null) {
                deleteItem(storedItem.getId());
            }
        }

        for (CartItem currentItem : currentItems) {
            CartItem storedItem = findItemInList(storedItems, currentItem.getId());
            if (storedItem == null) {
                // 새로 담은 항목만 INSERT
                insertItem(currentItem);
            } else if (storedItem.getQuantity() != currentItem.getQuantity()) {
                // 기존 항목은 수량만 UPDATE. 처음 복사한 이름·단가·재고는 그대로 둠
                updateQuantity(currentItem);
            }
        }

        // 새 행 번호가 부여된 경우에만 다음 번호 변경
        if (storedCart.getNextSequence() != cart.getNextSequence()) {
            updateNextSequence(cart.getNextSequence());
        }
    }

    private CartItem findItemInList(List<CartItem> items, Long id) {
        for (CartItem item : items) {
            if (item.getId().equals(id)) {
                return item;
            }
        }
        return null;
    }

    private void insertItem(CartItem item) {
        String sql = """
                INSERT INTO cart_items
                    (id, item_id, name, unit_price, source_type, stock, quantity)
                VALUES
                    (:id, :itemId, :name, :unitPrice, :sourceType, :stock, :quantity)
                """;

        MapSqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("id", item.getId())
                .addValue("itemId", item.getItemId())
                .addValue("name", item.getName())
                .addValue("unitPrice", item.getUnitPrice())
                .addValue("sourceType", item.getSourceType().name())
                .addValue("stock", item.getStock())
                .addValue("quantity", item.getQuantity());

        namedParameterJdbcTemplate.update(sql, parameters);
    }

    private void updateQuantity(CartItem item) {
        String sql = """
                UPDATE cart_items
                SET quantity = :quantity
                WHERE id = :id
                """;

        MapSqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("id", item.getId())
                .addValue("quantity", item.getQuantity());

        namedParameterJdbcTemplate.update(sql, parameters);
    }

    private void deleteItem(Long id) {
        String sql = "DELETE FROM cart_items WHERE id = :id";
        MapSqlParameterSource parameters = new MapSqlParameterSource("id", id);
        namedParameterJdbcTemplate.update(sql, parameters);
    }

    private void updateNextSequence(long nextSequence) {
        String sql = """
                UPDATE cart_state
                SET next_sequence = :nextSequence
                WHERE id = :id
                """;

        MapSqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("id", 1L)
                .addValue("nextSequence", nextSequence);

        namedParameterJdbcTemplate.update(sql, parameters);
    }

    private final RowMapper<CartItem> cartItemRowMapper =
            (resultSet, rowNum) -> new CartItem(
                    resultSet.getLong("id"),
                    resultSet.getLong("item_id"),
                    resultSet.getString("name"),
                    resultSet.getLong("unit_price"),
                    SourceType.valueOf(resultSet.getString("source_type")),
                    resultSet.getObject("stock", Integer.class),
                    resultSet.getInt("quantity")
            );
}