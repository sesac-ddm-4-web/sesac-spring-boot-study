package com.example.personal_project.infrastructure;

import com.example.personal_project.domain.EntityNotFoundException;
import com.example.personal_project.domain.Item;
import com.example.personal_project.domain.ItemRepository;
import com.example.personal_project.domain.SourceType;
import org.springframework.context.annotation.Profile;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@Profile("prod")
public class DatabaseItemRepository implements ItemRepository {

    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    public DatabaseItemRepository(
            NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
    }

    @Override
    public Item add(Item item) {
        // id는 DB가 생성하므로 INSERT 대상에서 제외
        String sql = """
            INSERT INTO items
                (name, price, source_type, external_id, stock)
            VALUES
                (:name, :price, :sourceType, :externalId, :stock)
            """;

        MapSqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("name", item.getName())
                .addValue("price", item.getPrice())
                .addValue("sourceType", item.getSourceType().name())
                .addValue("externalId", item.getExternalId())
                .addValue("stock", item.getStock());

        // DB가 생성한 번호를 받을 객체를 준비
        KeyHolder keyHolder = new GeneratedKeyHolder();
        try {
            namedParameterJdbcTemplate.update(
                    sql,
                    parameters,
                    keyHolder,
                    new String[]{"id"}
            );
        } catch (DuplicateKeyException exception) {
            throw new IllegalStateException(
                    "이미 가져온 상품입니다.",
                    exception
            );
        }

        Long generatedId = keyHolder.getKey().longValue();

        Item savedItem = new Item(
                generatedId,
                item.getName(),
                item.getPrice(),
                item.getSourceType(),
                item.getExternalId(),
                item.getStock()
        );

        return savedItem;
    }

    @Override
    public List<Item> findAll() {
        String sql = """
            SELECT id, name, price, source_type, external_id, stock
            FROM items
            ORDER BY id ASC
            """;
        // id ASC: DB가 생성한 id의 오름차순

        List<Item> items = namedParameterJdbcTemplate.query( // query(): 여러 행을 조회해서 List<Item>으로 받음
                sql,
                itemRowMapper // itemRowMapper: 조회한 각 행을 하나씩 Item으로 변환
        );

        return items;
    }

    @Override
    public Item findById(Long id) {
        String sql = """
            SELECT id, name, price, source_type, external_id, stock
            FROM items
            WHERE id = :id
            """;

        // parameters가 SQL의 :id에 전달받은 id를 연결
        MapSqlParameterSource parameters =
                new MapSqlParameterSource("id", id);

        try {
            Item item = namedParameterJdbcTemplate.queryForObject( // queryForObject()가 SQL을 실행해 한 행을 조회
                    sql,
                    parameters,
                    itemRowMapper // itemRowMapper가 조회한 행을 Item으로 변환
            );

            return item;
        } catch (EmptyResultDataAccessException exception) { // 조회 결과가 없으면
            throw new EntityNotFoundException("아이템을 찾지 못했습니다.");
        }
    }

    @Override
    public List<Item> findByNameContaining(String name) {
        String sql = """
            SELECT id, name, price, source_type, external_id, stock
            FROM items
            WHERE LOCATE(CAST(:name AS BINARY), CAST(name AS BINARY)) > 0
            ORDER BY id ASC
            """;

        MapSqlParameterSource parameters =
                new MapSqlParameterSource("name", name);

        List<Item> items = namedParameterJdbcTemplate.query(
                sql,
                parameters,
                itemRowMapper
        );

        return items;
    }

    @Override
    public boolean existsExternal(SourceType sourceType, Long externalId) {
        String sql = """
            SELECT COUNT(*)
            FROM items
            WHERE source_type = :sourceType
              AND external_id = :externalId
            """;

        MapSqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("sourceType", sourceType.name())
                .addValue("externalId", externalId);

        Long count = namedParameterJdbcTemplate.queryForObject(
                sql,
                parameters,
                Long.class
        );

        return count > 0; // 하나라도 있으면 true, 없으면 false
    }

    @Override
    public Item update(Item item) {
        String sql = """
            UPDATE items
            SET name = :name,
                price = :price
            WHERE id = :id
            """;

        MapSqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("id", item.getId())
                .addValue("name", item.getName())
                .addValue("price", item.getPrice());

        // 수정 대상 행 수
        int updatedCount = namedParameterJdbcTemplate.update(
                sql,
                parameters
        );

        if (updatedCount == 0) {
            throw new EntityNotFoundException("아이템을 찾지 못했습니다.");
        }

        return findById(item.getId());
    }

    @Override
    public void remove(Long id) {
        String sql = """
            DELETE FROM items
            WHERE id = :id
            """;

        MapSqlParameterSource parameters =
                new MapSqlParameterSource("id", id);

        int deletedCount = namedParameterJdbcTemplate.update(
                sql,
                parameters
        );

        if (deletedCount == 0) {
            throw new EntityNotFoundException("아이템을 찾지 못했습니다.");
        }
    }

    // DB에서 조회한 한 행을 Item 객체로 바꾸기
    private final RowMapper<Item> itemRowMapper = (resultSet, rowNum) ->
            new Item(
                    resultSet.getLong("id"),
                    resultSet.getString("name"),
                    resultSet.getInt("price"),
                    SourceType.valueOf(resultSet.getString("source_type")),
                    resultSet.getObject("external_id", Long.class),
                    resultSet.getObject("stock", Integer.class)
            );
}