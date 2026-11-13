package com.example.wishlist.management.application.wish;

import com.example.wishlist.management.domain.exception.ConflictException;
import com.example.wishlist.management.domain.wish.Wish;
import com.example.wishlist.management.domain.wish.WishRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class WishListService {
    private final WishRepository wishRepository;

    public WishListService(WishRepository wishRepository) {
        this.wishRepository = wishRepository;
    }

    public Wish add(Wish wish) {
        return wishRepository.add(wish);
    }

    public List<Wish> getWishList() {
        return wishRepository.getWishList();
    }

    public Wish getWish(long id) {
        return wishRepository.getWish(id);
    }

    public Wish updateWish(long id, Wish wish) {
        Wish existingWish = wishRepository.getWish(id);
        if(existingWish.isExternal()){
            throw new ConflictException("외부에서 가져온 상품은 수정할 수 없습니다.");
        }
        wish.setId(id);   // URL의 id 기준 처리
        return wishRepository.updateWish(wish);
    }

    public void deleteWish(long id) {
        wishRepository.deleteWish(id);
    }

    public List<Wish> getRecommendWishList(long budget) {
        return wishRepository.getRecommendWishList(budget);
    }
}