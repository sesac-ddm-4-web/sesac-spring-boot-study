package com.example.wishlist.management.presentation.wish;

import com.example.wishlist.management.dto.wish.WishDto;
import com.example.wishlist.management.application.wish.WishListService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class WishListController {
    private final WishListService wishListService;

    public WishListController(WishListService wishListService) {
        this.wishListService = wishListService;
    }

    // 위시리스트에 아이템 등록
    @PostMapping("/wish")
    public WishDto register(
            @Valid @RequestBody WishDto request)
    {
        return WishDto.from(wishListService.add(request.toEntity()));
    }

    // 전체 목록 조회
    @GetMapping("/wishes")
    public List<WishDto> getWishList() {
        return wishListService.getWishList().stream().map(WishDto::from).toList();
    }

    // 등록한 id의 아이템 조회
    @GetMapping("/wish/{id}")
    public WishDto getWish(@PathVariable @Min(1) long id) {
        return WishDto.from(wishListService.getWish(id));
    }

    // id 기반 수정
    @PutMapping("/wish/{id}")
    public WishDto updateWish(
            @PathVariable long id,
            @Valid @RequestBody WishDto request)
    {
        return WishDto.from(wishListService.updateWish(id, request.toEntity()));
    }

    // id 기반 삭제
    @DeleteMapping("/wish/{id}")
    public void deleteWish(@PathVariable @Min(1) long id) {
        wishListService.deleteWish(id);
    }

    // 예산 안에서 살 수 있는 아이템을 최대 3개 추천
    @GetMapping("/wish/recommend/{budget}")
    public List<WishDto> getRecommendWishList(@PathVariable @Min(0) long budget) {
        return wishListService.getRecommendWishList(budget).stream().map(WishDto::from).toList();
    }
}