package com.example.wishlist.management.infrastructure.wish;

import com.example.wishlist.management.domain.exception.ConflictException;
import com.example.wishlist.management.domain.exception.NotFoundException;
import com.example.wishlist.management.domain.wish.SourceType;
import com.example.wishlist.management.domain.wish.Wish;
import com.example.wishlist.management.domain.wish.WishRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
@Primary
public class DatabaseWishRepository implements WishRepository {

    private static final RowMapper<Wish> WISH_MAPPER = (rs, rowNum) -> new Wish(
            rs.getLong("id"),
            rs.getString("name"),
            rs.getInt("price"),
            SourceType.valueOf(rs.getString("source_type")),
            rs.getObject("external_id", Long.class),
            rs.getObject("stock", Integer.class));

    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    @Autowired
    public DatabaseWishRepository(NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
    }

    // 등록
    @Override
    public Wish add(Wish wish) {
        String sql = "INSERT INTO wish (name, price, source_type, external_id, stock) "
                + "VALUES (:name, :price, :sourceType, :externalId, :stock)";

        SqlParameterSource namedParameters = new MapSqlParameterSource()
                .addValue("name", wish.getName())
                .addValue("price", wish.getPrice())
                .addValue("sourceType", wish.getSourceType().name())
                .addValue("externalId", wish.getExternalId())
                .addValue("stock", wish.getStock());

        KeyHolder keyHolder = new GeneratedKeyHolder();
        try {
            namedParameterJdbcTemplate.update(sql, namedParameters, keyHolder);
        } catch (DuplicateKeyException e) {
            throw new ConflictException(
                    "이미 위시에 등록된 외부 상품입니다. externalId=" + wish.getExternalId());
        }

        long newId = keyHolder.getKey().longValue();
        return getWish(newId);
    }

    // 전체 조회
    @Override
    public List<Wish> getWishList() {
        return namedParameterJdbcTemplate.query(
                "SELECT * FROM wish ORDER BY id", WISH_MAPPER);
    }

    // id 기반 조회
    @Override
    public Wish getWish(long id) {
        List<Wish> result = namedParameterJdbcTemplate.query(
                "SELECT * FROM wish WHERE id = :id",
                Map.of("id", id),
                WISH_MAPPER);
        if (result.isEmpty()) {
            throw new NotFoundException("위시를 찾을 수 없습니다. id=" + id);
        }
        return result.get(0);
    }

    // 수정
    // id로 상품을 찾고 수정하는 기능
    @Override
    public Wish updateWish(Wish wish) {
        SqlParameterSource params = new MapSqlParameterSource()
                .addValue("id", wish.getId())
                .addValue("name", wish.getName())
                .addValue("price", wish.getPrice())
                .addValue("stock", wish.getStock());

        namedParameterJdbcTemplate.update(
                "UPDATE wish SET name = :name, price = :price, stock = :stock WHERE id = :id",
                params);
        return getWish(wish.getId());
    }

    // id 기반 삭제
    @Override
    public void deleteWish(long id) {
        int deleted = namedParameterJdbcTemplate.update(
                "DELETE FROM wish WHERE id = :id", Map.of("id", id));
        if (deleted == 0) {
            throw new NotFoundException("위시를 찾을 수 없습니다. id=" + id);
        }
    }

    // 추천
    @Override
    public List<Wish> getRecommendWishList(long budget) {
        // TODO: ListWishRepository의 추천 로직을 옮길 것
        return List.of();
    }

    // 외부 id 중복 검사
    @Override
    public boolean existByExternalId(long externalId) {
        Integer count = namedParameterJdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM wish WHERE external_id = :externalId",
                Map.of("externalId", externalId),
                Integer.class);
        return count != null && count > 0;
    }
}