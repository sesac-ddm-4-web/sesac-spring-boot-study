package com.example.personal_project.presentation;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;


// 수량 변경 요청 DTO
public record ChangeQuantityRequest (
        @NotNull
        @Min(1)
        @Max(5)
        Integer quantity
){
}
