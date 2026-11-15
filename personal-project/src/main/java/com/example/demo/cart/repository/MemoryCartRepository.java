package com.example.demo.cart.repository;

import com.example.demo.cart.domain.Cart;
import com.example.demo.cart.domain.CartItem;
import com.example.demo.global.exception.EntityNotFoundException;
import com.example.demo.item.domain.CollectionItem;
import com.example.demo.item.repository.CollectionItemRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
@Profile("test")
public class MemoryCartRepository implements CartRepository {

    private record Row(Long id, Long itemId, int quantity) {
    }

    private final CollectionItemRepository collectionItemRepository;
    private List<Row> rows = new ArrayList<>();

    public MemoryCartRepository(CollectionItemRepository collectionItemRepository) {
        this.collectionItemRepository = collectionItemRepository;
    }

    @Override
    public Cart find() {
        List<CartItem> items = new ArrayList<>();

        for (Row row : rows) {
            CollectionItem item;
            try {
                item = collectionItemRepository.findById(row.itemId());
            } catch (EntityNotFoundException e) {
                continue;
            }
            items.add(CartItem.restore(
                    row.id(), row.itemId(), item.getName(), item.getPrice(), item.getStock(), row.quantity()));
        }

        return Cart.restore(items);
    }

    @Override
    public void save(Cart cart) {
        rows = cart.getItems().stream()
                .map(cartItem -> new Row(cartItem.getId(), cartItem.getItemId(), cartItem.getQuantity()))
                .toList();
    }
}
