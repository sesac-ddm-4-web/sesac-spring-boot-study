package com.example.personal_project.application;

import com.example.personal_project.domain.repository.CollectionItemRepository;
import com.example.personal_project.domain.repository.ShoppingCartRepository;
import com.example.personal_project.domain.entity.CollectionItem;
import com.example.personal_project.domain.entity.ShoppingCart;
import com.example.personal_project.domain.exception.CommonNotFoundException;
import com.example.personal_project.presentation.dto.shoppingCart.ShoppingCartRequest;
import com.example.personal_project.presentation.dto.shoppingCart.ShoppingCartResponse;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ShoppingCartService {
    private final ShoppingCartRepository shoppingCartRepository;
    private final CollectionItemRepository collectionItemRepository;

    public ShoppingCartService(ShoppingCartRepository shoppingCartRepository, CollectionItemRepository collectionItemRepository) {
        this.shoppingCartRepository = shoppingCartRepository;
        this.collectionItemRepository = collectionItemRepository;
    }

    public ShoppingCartResponse addCart(ShoppingCartRequest shoppingCartRequest){
        CollectionItem collectionItem = collectionItemRepository.findById(shoppingCartRequest.collectionItemId());
        ShoppingCart shoppingCart = ShoppingCart.createShoppingCart(collectionItem, shoppingCartRequest);
        if(shoppingCartRepository.existByCollectionItemId(collectionItem.getId())){
            ShoppingCart existingCart = shoppingCartRepository.findByCollectionItemId(collectionItem.getId());
            Integer prevAmount = existingCart.getAmount();
            ShoppingCartRequest reviseRequest = new ShoppingCartRequest(collectionItem.getId(), prevAmount+shoppingCartRequest.amount());
            return editCart(reviseRequest);
        }
        ShoppingCart createdShoppingCart = shoppingCartRepository.add(shoppingCart);
        return ShoppingCartResponse.form(createdShoppingCart);
    }

    public ShoppingCartResponse editCart(ShoppingCartRequest shoppingCartRequest){
        if(!shoppingCartRepository.existByCollectionItemId(shoppingCartRequest.collectionItemId()))
            throw new CommonNotFoundException("장바구니에 해당 상품이 없습니다");
        CollectionItem collectionItem =
                collectionItemRepository.findById(shoppingCartRequest.collectionItemId());
        ShoppingCart shoppingCart = ShoppingCart.createShoppingCart(collectionItem,shoppingCartRequest);
        shoppingCartRepository.update(shoppingCart);
        return ShoppingCartResponse.form(shoppingCart);
    }

    public List<ShoppingCartResponse> findAll(){
        return shoppingCartRepository.findAll().stream()
                .map(ShoppingCartResponse::form)
                .toList();
    }

    public void delete(Long collectionItemId){
        shoppingCartRepository.delete(collectionItemId);
    }

}
