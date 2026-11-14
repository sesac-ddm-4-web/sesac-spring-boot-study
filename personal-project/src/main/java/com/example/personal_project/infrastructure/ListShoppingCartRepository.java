package com.example.personal_project.infrastructure;

import com.example.personal_project.domain.repository.ShoppingCartRepository;
import com.example.personal_project.domain.entity.ShoppingCart;
import com.example.personal_project.domain.exception.CommonConflictException;
import com.example.personal_project.domain.exception.CommonNotFoundException;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

@Repository
public class ListShoppingCartRepository implements ShoppingCartRepository {
    List<ShoppingCart> shoppingCarts = new CopyOnWriteArrayList<>();

    public ShoppingCart add(ShoppingCart shoppingCart){
        if(shoppingCarts.size() == ShoppingCart.MAX_CART_ITEM)
            throw new CommonConflictException("장바구니 아이템은 최대 20개입니다");
        shoppingCarts.add(shoppingCart);
        return shoppingCart;
    }

    public ShoppingCart findByCollectionItemId(Long collectionItemId){
        return shoppingCarts.stream()
                .filter(shoppingCart -> shoppingCart.sameCollectionItemId(collectionItemId))
                .findFirst()
                .orElseThrow(()->new CommonNotFoundException("장바구니에 해당 item이 존재하지 않습니다"));
    }

    public Boolean existByCollectionItemId(Long collectionItemId){
        return shoppingCarts.stream()
                .anyMatch(shoppingCart ->
                        Objects.equals(shoppingCart.getCollectionItemId(),collectionItemId));
    }

    public ShoppingCart update(ShoppingCart shoppingCart){
        Integer indexToModify = shoppingCarts.indexOf(shoppingCart);
        shoppingCarts.set(indexToModify,shoppingCart);
        return shoppingCart;
    }

    public List<ShoppingCart> findAll(){
        return shoppingCarts;
    }

    public void delete(Long collectionItemId){
        ShoppingCart shoppingCart = findByCollectionItemId(collectionItemId);
        shoppingCarts.remove(shoppingCart);
    }

}
