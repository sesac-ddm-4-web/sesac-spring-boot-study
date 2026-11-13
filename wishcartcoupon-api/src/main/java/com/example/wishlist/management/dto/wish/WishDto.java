package com.example.wishlist.management.dto.wish;

import com.example.wishlist.management.domain.wish.Wish;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record WishDto(Long id,
                      @NotBlank(message = "이름은 필수입니다.")  String name,
                      @NotNull @PositiveOrZero Integer price) {

    // 도메인 → DTO (응답용)
    public static WishDto from(Wish wish) {
        return new WishDto(wish.getId(), wish.getName(), wish.getPrice());
    }

    // DTO → 도메인 (요청용)
    public Wish toEntity() {
        return new Wish(id == null ? 0 : id, name, price);
    }
}