package com.example.hobby_wishlist.infrastructure.persistence.database;

import com.example.hobby_wishlist.domain.wishlist.SourceType;
import com.example.hobby_wishlist.domain.wishlist.WishlistItem;
import com.example.hobby_wishlist.domain.wishlist.WishlistItemInCartException;
import com.example.hobby_wishlist.domain.wishlist.WishlistRepository;
import org.jspecify.annotations.Nullable;
import org.springframework.context.annotation.Profile;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
@Profile("prod")
public class DatabaseWishlistRepository implements WishlistRepository {
    private final NamedParameterJdbcTemplate jdbcTemplate;

    private final RowMapper<WishlistItem> wishlistItemRowMapper = (rs, rowNum) -> {
        Long id = rs.getLong("id");
        SourceType sourceType = SourceType.valueOf(rs.getString("source_type"));
        String name = rs.getString("name");
        Integer price = rs.getInt("price");
        Long externalId = rs.getLong("external_id");
        Integer stock = rs.getInt("stock");

        return switch (sourceType) {
            case MANUAL -> WishlistItem.fromManual(id, name, price);
            case EXTERNAL -> WishlistItem.fromExternal(id, externalId, name, price, stock);
        };
    };

    public DatabaseWishlistRepository(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public WishlistItem add(WishlistItem item) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        SqlParameterSource sqlParameterSource = new BeanPropertySqlParameterSource(item) {
            @Override
            public @Nullable Object getValue(String paramName) {
                Object value = super.getValue(paramName);
                // WishlistItem의 sourceType이 열거형이고
                // DB에는 문자열(varchar)로 저장되므로
                // Enum인 경우 문자열로 값을 변환함
                return value instanceof Enum<?> e ? e.name() : value;
            }
        };

        jdbcTemplate.update(
                """
                        INSERT INTO wishlist_items
                            (source_type, external_id, name, price, stock)
                        VALUES(:sourceType, :externalId, :name, :price, :stock)
                        """,
                sqlParameterSource,
                keyHolder
        );

        Long generatedId = keyHolder.getKey().longValue();
        item.setId(generatedId);

        return item;
    }

    @Override
    public List<WishlistItem> getAll() {
        return jdbcTemplate.query(
                "SELECT * FROM wishlist_items",
                wishlistItemRowMapper
        );
    }

    @Override
    public WishlistItem findById(Long id) {
        return jdbcTemplate.queryForObject(
                """
                        SELECT * FROM wishlist_items 
                                 WHERE id=:id
                        """,
                new MapSqlParameterSource("id", id),
                wishlistItemRowMapper
        );
    }

    @Override
    public List<WishlistItem> recommend(Integer budget) {
        return jdbcTemplate.query(
                """
                        SELECT * FROM wishlist_items
                                 WHERE price <= :budget
                                 ORDER BY price ASC, id ASC
                                 LIMIT 3
                        """,
                new MapSqlParameterSource("budget", budget),
                wishlistItemRowMapper
        );
    }


    @Override
    @Transactional
    public void remove(Long id) {
        try {
            jdbcTemplate.update(
                    """
                            DELETE FROM wishlist_items
                            WHERE id = :id
                            """,
                    new MapSqlParameterSource("id", id)
            );
        } catch (DataIntegrityViolationException e) {      // FK 위반
            throw new WishlistItemInCartException("장바구니에 담긴 상품은 삭제할 수 없습니다. id=%d".formatted(id));
        }
    }

    @Override
    public boolean existsByExternalId(Long externalId) {
        return jdbcTemplate.queryForObject(
                """
                        SELECT EXISTS(
                            SELECT 1 FROM wishlist_items
                                     WHERE external_id = :externalId
                        )
                        """,
                new MapSqlParameterSource("externalId", externalId),
                Boolean.class
        );
    }
}
