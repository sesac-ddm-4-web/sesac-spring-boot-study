package com.example.wishlist.item.repository;

import com.example.wishlist.item.domain.SourceType;
import com.example.wishlist.item.domain.WishlistItem;
import com.example.wishlist.item.exception.DuplicateImportException;
import java.util.List;
import java.util.Optional;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

@Repository
public class DatabaseWishlistItemRepository implements WishlistItemRepository {

    private static final String SELECT_COLUMNS = "SELECT id, name, price, source_type, external_id, stock FROM wishlist_item";
    private static final String INSERT_SQL = """
            INSERT INTO wishlist_item (name, price, source_type, external_id, stock)
            VALUES (:name, :price, :sourceType, :externalId, :stock)
            """;
    private static final String UPDATE_SQL = "UPDATE wishlist_item SET name = :name, price = :price WHERE id = :id";
    private static final String DELETE_SQL = "DELETE FROM wishlist_item WHERE id = :id";

    private static final RowMapper<WishlistItem> ROW_MAPPER = (rs, rowNum) -> WishlistItem.restore(
        rs.getLong("id"),
        rs.getString("name"),
        rs.getInt("price"),
        SourceType.valueOf(rs.getString("source_type")),
        rs.getObject("external_id", Long.class),
        rs.getObject("stock", Integer.class)
    );

    private final NamedParameterJdbcTemplate template;

    public DatabaseWishlistItemRepository(NamedParameterJdbcTemplate template) {
        this.template = template;
    }


    @Override
    public WishlistItem save(WishlistItem item) {
        SqlParameterSource params = new MapSqlParameterSource()
            .addValue("name", item.getName())
            .addValue("price", item.getPrice())
            .addValue("sourceType", item.getSourceType().name())
            .addValue("externalId", item.getExternalId())
            .addValue("stock", item.getStock());

        KeyHolder keyHolder = new GeneratedKeyHolder();

        try {
            template.update(INSERT_SQL, params, keyHolder, new String[]{"id"});
        } catch (DuplicateKeyException e) {
            if (item.getExternalId() != null) {
                throw new DuplicateImportException(item.getExternalId());
            }
            throw e;
        }

        return item.withId(keyHolder.getKey().longValue());
    }

    @Override
    public WishlistItem update(WishlistItem item) {
        SqlParameterSource params = new MapSqlParameterSource()
            .addValue("name", item.getName())
            .addValue("price", item.getPrice())
            .addValue("id", item.getId());

        int updated = template.update(UPDATE_SQL, params);
        if (updated == 0) {
            throw new IllegalArgumentException("저장되지 않은 아이템은 수정할 수 없습니다. id=" + item.getId());
        }
        return item;

    }

    @Override
    public boolean deleteById(Long id) {
        return template.update(DELETE_SQL, new MapSqlParameterSource("id", id)) > 0;
    }

    @Override
    public List<WishlistItem> findAll() {
        return List.copyOf(template.query(SELECT_COLUMNS + " ORDER BY id", ROW_MAPPER));
    }

    @Override
    public Optional<WishlistItem> findById(Long id) {
        return findOne(SELECT_COLUMNS + " WHERE id = :id", new MapSqlParameterSource("id", id));
    }

    @Override
    public Optional<WishlistItem> findByExternalId(Long externalId) {
        return findOne(SELECT_COLUMNS + " WHERE external_id = :externalId", new MapSqlParameterSource("externalId", externalId));    }

    private Optional<WishlistItem> findOne (String sql, SqlParameterSource params) {
        return template.query(sql, params, ROW_MAPPER).stream().findFirst();
    }
}
