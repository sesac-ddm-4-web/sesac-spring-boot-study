package com.example.wishlist.management.infrastructure.wish;

import com.example.wishlist.management.domain.wish.Wish;
import com.example.wishlist.management.domain.wish.WishRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Repository;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class ListWishRepository implements WishRepository {

    private List<Wish> listWish = new ArrayList<>();
    private AtomicLong sequence = new AtomicLong(1L);

    // 위리리스테 아이템 등록 & 가격 오름차순으로 정렬 & 같은 가격은 등록 순서 유지
    public Wish add(Wish wish){
        // 프로그램 고유의 id로 초기화
        wish.setId(sequence.getAndIncrement());

        listWish.add(wish);
        sortByPrice();
        return wish;
    }

    // 전체 목록 조회
    public List<Wish> getWishList(){
        // 외부에서 변경하지 못 하도록 복사본 반환
        return new ArrayList<>(listWish);
    }

    // 등록한 id의 아이템 조회
    public Wish getWish(long id){
        return listWish.stream()
                .filter(wish -> wish.sameId(id))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "getWish, 해당 아이템이 없습니다. id=" + id));
    }

    // id 기반 수정
    public Wish updateWish(Wish wish){
        // id 기반으로 현재 위시를 알아내고
        Wish existingWish = getWish(wish.getId());
        // list에 어디에 위치하는 지 알아내고
        int index = listWish.indexOf(existingWish);
        // 그 위치를 기반으로 수정할 wish로 업데이트한다.
        listWish.set(index, wish);
        // 가격이 바뀔 수 있으므로 다시 정렬
        sortByPrice();
        return wish;
    }

    // id 기반 삭제
    public void deleteWish(long id){
        Wish existingWish = listWish.stream()
                .filter(wish -> wish.sameId(id))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "deleteWish, 삭제하고 싶은 아이템은 현재 목록에 없습니다. id=" + id));
        listWish.remove(existingWish);
    }

    // 예산 안에서 살 수 있는 아이템을 최대 3개 추천
    public List<Wish> getRecommendWishList(long budget){
        // 예산 0 미만 예외
        if (budget < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "getRecommendWishList, 예산은 0 이상이어야 합니다. budget=" + budget);
        }
        return listWish.stream()
                .filter(wish -> wish.getPrice() <= budget)
                .limit(3)
                .toList();
    }

    // 가격 오름차순 정렬
    private void sortByPrice(){
        listWish.sort(Comparator.comparing(Wish::getPrice));
    }

    // 새롭게 추가: 위시리스트로 가져오기
    @Override
    public boolean existByExternalId(long externalId) {
        return listWish.stream().anyMatch(wish -> wish.sameExternalId(externalId));
    }
}
