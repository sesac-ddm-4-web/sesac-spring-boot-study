
package com.example.campkit.service;

import com.example.campkit.domain.*;
import com.example.campkit.exception.*;
import com.example.campkit.external.*;
import com.example.campkit.repository.CartRepository;
import com.example.campkit.repository.CollectionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class CartServiceTest {

    private ExternalProductClient client;
    private CollectionRepository collectionRepository;
    private CartRepository cartRepository;

    private CollectionService collections;
    private CartService cart;

    private final Map<Long, CollectionItem> collectionItems = new LinkedHashMap<>();

    private final Map<Long, CartItem> cartItems = new LinkedHashMap<>();

    @BeforeEach
    void setUp() {
        collectionItems.clear();
        cartItems.clear();

        client = mock(ExternalProductClient.class);
        collectionRepository = mock(CollectionRepository.class);
        cartRepository = mock(CartRepository.class);

        long[] collectionId = {1L};
        long[] cartId = {1L};

        // 캠핑 장비 저장
        when(collectionRepository.save(any(CollectionItem.class)))
                .thenAnswer(invocation -> {
                    CollectionItem item = invocation.getArgument(0);
                    CollectionItem saved = item.withId(collectionId[0]++);
                    collectionItems.put(saved.getId(), saved);
                    return saved;
                });

        // 캠핑 장비 ID 조회
        when(collectionRepository.findById(anyLong()))
                .thenAnswer(invocation -> {
                    Long id = invocation.getArgument(0);
                    return Optional.ofNullable(collectionItems.get(id));
                });

        // 외부 상품 중복 조회
        when(collectionRepository.findByExternalId(anyLong()))
                .thenAnswer(invocation -> {
                    Long externalId = invocation.getArgument(0);

                    return collectionItems.values().stream()
                            .filter(item -> item.getSourceType() == SourceType.DUMMYJSON)
                            .filter(item -> Objects.equals(
                                    item.getExternalId(), externalId))
                            .findFirst();
                });

        // 장바구니 저장
        when(cartRepository.saveNew(any(CartItem.class)))
                .thenAnswer(invocation -> {
                    CartItem item = invocation.getArgument(0);
                    CartItem saved = item.withId(cartId[0]++);
                    cartItems.put(saved.getId(), saved);
                    return saved;
                });

        // 장바구니 전체 조회
        when(cartRepository.findAll())
                .thenAnswer(invocation ->
                        new ArrayList<>(cartItems.values()));

        // 장바구니 ID 조회
        when(cartRepository.findById(anyLong()))
                .thenAnswer(invocation -> {
                    Long id = invocation.getArgument(0);
                    return Optional.ofNullable(cartItems.get(id));
                });

        // 동일 상품 조회
        when(cartRepository.findByCollectionItemId(anyLong()))
                .thenAnswer(invocation -> {
                    Long id = invocation.getArgument(0);

                    return cartItems.values().stream()
                            .filter(item -> item.getCollectionItemId() == id)
                            .findFirst();
                });

        // 수량 수정
        doAnswer(invocation -> {
            CartItem item = invocation.getArgument(0);
            cartItems.put(item.getId(), item);
            return null;
        }).when(cartRepository).replace(any(CartItem.class));

        // 장바구니 삭제
        doAnswer(invocation -> {
            Long id = invocation.getArgument(0);
            cartItems.remove(id);
            return null;
        }).when(cartRepository).delete(anyLong());

        collections = new CollectionService(
                collectionRepository,
                new ExternalProductService(client)
        );

        cart = new CartService(
                cartRepository,
                collections
        );
    }

    @Test
    void 동일상품_수량합산() {
        long id = collections.register("랜턴", 20000).getId();

        cart.add(id, 2);
        cart.add(id, 1);
        assertEquals(1, cart.findAll().size());
        assertEquals(3, cart.findAll().get(0).getQuantity());
    }

    @Test
    void 최대수량_초과검증() {
        long id = collections.register("의자", 10000).getId();

        cart.add(id, 3);

        assertThrows(ConflictException.class, () -> cart.add(id, 3));
        assertEquals(3, cart.findAll().get(0).getQuantity());
    }

    @Test
    void 품절상품_등록방지() {
        when(client.findById(7)).thenReturn(
                new DummyJsonProduct(
                        7L,
                        "텐트",
                        new BigDecimal("10"),
                        "camp",
                        null,
                        0
                )
        );

        CollectionItem item = collections.importExternal(7);

        assertThrows(
                ConflictException.class,
                () -> cart.add(item.getId(), 1)
        );

        assertTrue(cart.findAll().isEmpty());
    }

    @Test
    void 쿠폰할인_배송비계산() {
        long id = collections.register("텐트", 110000).getId();

        cart.add(id, 1);

        assertEquals(110000,
                cart.preview("NONE").finalAmount());

        assertEquals(104000,
                cart.preview("PERCENT10").finalAmount());

        assertEquals(100000,
                cart.preview("FIXED15000").finalAmount());
    }

    @Test
    void 최소주문금액_검증() {
        long id = collections.register("랜턴", 50000).getId();

        cart.add(id, 1);

        assertThrows(
                IllegalArgumentException.class,
                () -> cart.preview("FIXED15000")
        );
    }

    @Test
    void 최대할인금액_검증() {
        long id = collections.register("텐트", 250000).getId();

        cart.add(id, 1);
        assertEquals(20000, cart.preview("PERCENT10").discount());
    }

    @Test
    void 장바구니_상품삭제() {
        long id = collections.register("의자", 20000).getId();

        CartItem item = cart.add(id, 1);

        cart.delete(item.getId());

        assertTrue(cart.findAll().isEmpty());
    }
}
