
package com.example.campkit.repository;

import com.example.campkit.domain.CartItem;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.List;
import java.util.Optional;

@Repository
public class JdbcCartRepository implements CartRepository {

    private final JdbcTemplate jdbcTemplate;

    public JdbcCartRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private CartItem mapRow(ResultSet rs) throws SQLException {
        return new CartItem(
                rs.getLong("id"),
                rs.getLong("collection_item_id"),
                rs.getString("name"),
                rs.getLong("unit_price"),
                rs.getObject("stock", Integer.class),
                rs.getInt("quantity")
        );
    }

    @Override
    public List<CartItem> findAll() {
        String sql = "SELECT * FROM cart_items ORDER BY id";

        return jdbcTemplate.query(sql,
                (rs, rowNum) -> mapRow(rs));
    }

    @Override
    public Optional<CartItem> findById(long id) {
        String sql = "SELECT * FROM cart_items WHERE id = ?";

        return jdbcTemplate.query(sql,
                        (rs, rowNum) -> mapRow(rs), id)
                .stream()
                .findFirst();
    }

    @Override
    public Optional<CartItem> findByCollectionItemId(long id) {
        String sql = """
                SELECT * FROM cart_items
                WHERE collection_item_id = ?
                """;

        return jdbcTemplate.query(sql,
                        (rs, rowNum) -> mapRow(rs), id)
                .stream()
                .findFirst();
    }

    @Override
    public CartItem saveNew(CartItem item) {
        String sql = """
                INSERT INTO cart_items
                (collection_item_id, name, unit_price, stock, quantity)
                VALUES (?, ?, ?, ?, ?)
                """;

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(
                    sql, new String[]{"id"});

            ps.setLong(1, item.getCollectionItemId());
            ps.setString(2, item.getName());
            ps.setLong(3, item.getUnitPrice());

            if (item.getStock() == null) {
                ps.setNull(4, Types.INTEGER);
            } else {
                ps.setInt(4, item.getStock());
            }

            ps.setInt(5, item.getQuantity());
            return ps;
        }, keyHolder);

        Number key = keyHolder.getKey();

        if (key == null) {
            throw new IllegalStateException("장바구니 ID 생성 실패");
        }

        return item.withId(key.longValue());
    }

    @Override
    public void replace(CartItem item) {
        String sql = """
                UPDATE cart_items
                SET collection_item_id = ?,
                    name = ?,
                    unit_price = ?,
                    stock = ?,
                    quantity = ?
                WHERE id = ?
                """;
        jdbcTemplate.update(sql,
                item.getCollectionItemId(),
                item.getName(),
                item.getUnitPrice(),
                item.getStock(),
                item.getQuantity(),
                item.getId());
    }

    @Override
    public void delete(long id) {
        String sql = "DELETE FROM cart_items WHERE id = ?";
        jdbcTemplate.update(sql, id);
    }
}
