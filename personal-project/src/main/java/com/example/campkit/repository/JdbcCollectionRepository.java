
package com.example.campkit.repository;

import com.example.campkit.domain.CollectionItem;
import com.example.campkit.domain.SourceType;
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
public class JdbcCollectionRepository implements CollectionRepository {

    private final JdbcTemplate jdbcTemplate;

    public JdbcCollectionRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private CollectionItem mapRow(ResultSet rs) throws SQLException {
        return new CollectionItem(
                rs.getLong("id"),
                rs.getString("name"),
                rs.getLong("price"),
                SourceType.valueOf(rs.getString("source_type")),
                rs.getObject("external_id", Long.class),
                rs.getObject("stock", Integer.class)
        );
    }

    @Override
    public CollectionItem save(CollectionItem item) {
        String sql = """
                INSERT INTO collection_items
                (name, price, source_type, external_id, stock)
                VALUES (?, ?, ?, ?, ?)
                """;

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(
                    sql, new String[]{"id"});

            ps.setString(1, item.getName());
            ps.setLong(2, item.getPrice());
            ps.setString(3, item.getSourceType().name());

            if (item.getExternalId() == null) {
                ps.setNull(4, Types.BIGINT);
            } else {
                ps.setLong(4, item.getExternalId());
            }

            if (item.getStock() == null) {
                ps.setNull(5, Types.INTEGER);
            } else {
                ps.setInt(5, item.getStock());
            }

            return ps;
        }, keyHolder);

        Number key = keyHolder.getKey();

        if (key == null) {
            throw new IllegalStateException("장비 ID 생성 실패");
        }

        return item.withId(key.longValue());
    }

    @Override
    public List<CollectionItem> findAll() {
        String sql = "SELECT * FROM collection_items ORDER BY id";

        return jdbcTemplate.query(sql,
                (rs, rowNum) -> mapRow(rs));
    }

    @Override
    public Optional<CollectionItem> findById(Long id) {
        String sql = "SELECT * FROM collection_items WHERE id = ?";

        return jdbcTemplate.query(sql,
                        (rs, rowNum) -> mapRow(rs), id)
                .stream()
                .findFirst();
    }

    @Override
    public Optional<CollectionItem> findByExternalId(Long externalId) {
        String sql = """
                SELECT * FROM collection_items
                WHERE source_type = ?
                AND external_id = ?
                """;

        return jdbcTemplate.query(sql,
                        (rs, rowNum) -> mapRow(rs),
                        SourceType.DUMMYJSON.name(), externalId)
                .stream()
                .findFirst();
    }
    @Override
    public void delete(Long id) {
        String sql = "DELETE FROM collection_items WHERE id = ?";
        jdbcTemplate.update(sql, id);
    }
}
