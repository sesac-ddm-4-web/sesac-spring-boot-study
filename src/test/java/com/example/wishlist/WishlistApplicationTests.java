package com.example.wishlist;

import com.example.wishlist.item.controller.WishlistController;
import com.example.wishlist.item.domain.WishlistItem;
import com.example.wishlist.item.dto.ItemCreateRequest;
import com.example.wishlist.item.dto.ItemResponse;
import com.example.wishlist.item.dto.RecommendationResponse;
import com.example.wishlist.item.exception.ItemNotFoundException;
import com.example.wishlist.item.repository.WishlistItemRepository;
import com.example.wishlist.item.repository.DatabaseWishlistItemRepository;
import com.example.wishlist.item.service.WishlistService;
import com.example.wishlist.support.DatabaseCleaner;
import java.net.URI;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
class WishlistApplicationTests {

	@Autowired
	WishlistService service;

	@Autowired
	WishlistController controller;

	@Autowired
	WishlistItemRepository repository;

	@Autowired
	JdbcTemplate jdbcTemplate;

	/** DB(위시리스트·장바구니)는 테스트가 끝나도 데이터가 남으므로, 매 테스트 전에 비웁니다. */
	@BeforeEach
	void clearTable() {
		DatabaseCleaner.clear(jdbcTemplate);
	}

	@Test
	@DisplayName("Spring이 Repository interface에 DB 구현체를 주입한다")
	void repositoryIsWired() {
		assertThat(repository).isInstanceOf(DatabaseWishlistItemRepository.class);
	}

	@Test
	@DisplayName("Spring이 연결한 Service로 등록 > 조회 > 추천 동작한다")
	void serviceFlowWithSpringBeans() {
		WishlistItem saved = service.register("지구 끝의 온실", 18000);
		service.register("무료 전자책 샘플", 0);
		service.register("어린 왕자", 5000);

		assertThat(service.findById(saved.getId()).getName()).isEqualTo("지구 끝의 온실");
		assertThat(repository.findAll()).hasSize(3);

		List<WishlistItem> result = service.recommend(8000);
		assertThat(result).extracting(WishlistItem::getName)
			.containsExactly("무료 전자책 샘플", "어린 왕자");
	}

	@Test
	@DisplayName("Controller > Service > Repository 연동 성공, ResponseEntity로 상태 코드와 DTO 응답")
	void progressSuccess() {
		ResponseEntity<ItemResponse> created = controller.register(new ItemCreateRequest(" 지구 끝의 온실 ", 18000));
		controller.register(new ItemCreateRequest("무료 전자책 샘플", 0));
		ItemResponse saved = created.getBody();

		// 등록: 201 Created + Location 헤더
		assertThat(created.getStatusCode()).isEqualTo(HttpStatus.CREATED);
		assertThat(created.getHeaders().getLocation()).isEqualTo(URI.create("/api/wishlist/items/" + saved.id()));
		assertThat(saved.name()).isEqualTo("지구 끝의 온실");

		// 조회: 200 OK
		ResponseEntity<ItemResponse> found = controller.findById(saved.id());
		assertThat(found.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(found.getBody().price()).isEqualTo(18000);
		assertThat(controller.findAll().getBody()).hasSize(2);
		assertThatThrownBy(() -> controller.findById(999L))
			.isInstanceOf(ItemNotFoundException.class);

		RecommendationResponse recommendation = controller.recommend(10000).getBody();
		assertThat(recommendation.budget()).isEqualTo(10000);
		assertThat(recommendation.count()).isEqualTo(1);
		assertThat(recommendation.items()).extracting(ItemResponse::name)
			.containsExactly("무료 전자책 샘플");
	}

	@Test
	@DisplayName("삭제하면 204 No Content이고 본문이 없다")
	void deleteReturnsNoContent() {
		ItemResponse saved = controller.register(new ItemCreateRequest("데미안", 12000)).getBody();

		ResponseEntity<Void> deleted = controller.delete(saved.id());

		assertThat(deleted.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
		assertThat(deleted.getBody()).isNull();
		assertThat(controller.findAll().getBody()).isEmpty();
	}

}