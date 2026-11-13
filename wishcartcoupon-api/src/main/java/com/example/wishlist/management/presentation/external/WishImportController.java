package com.example.wishlist.management.presentation.external;

import com.example.wishlist.management.application.external.WishImportService;
import com.example.wishlist.management.dto.external.WishImportRequest;
import com.example.wishlist.management.dto.wish.WishDto;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class WishImportController {
    private final WishImportService wishImportService;

    public WishImportController(WishImportService wishImportService) {
        this.wishImportService = wishImportService;
    }

    // 외부 상품을 내부 위시리스트로 가져오기
    @PostMapping("/wish/import")
    @ResponseStatus(HttpStatus.CREATED) //201
    public WishDto importWish(@Valid @RequestBody WishImportRequest request){
        return WishDto.from(wishImportService.importWish(request.externalId()));
    }
}
