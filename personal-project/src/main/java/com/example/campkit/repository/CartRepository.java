
package com.example.campkit.repository;

import com.example.campkit.domain.CartItem;
import java.util.List;
import java.util.Optional;

public interface CartRepository {

    List<CartItem> findAll();

    Optional<CartItem> findById(long id);

    Optional<CartItem> findByCollectionItemId(long collectionItemId);

    CartItem saveNew(CartItem item);

    void replace(CartItem item);

    void delete(long id);
}
