package com.example.hobby_wishlist;

import com.example.hobby_wishlist.infrastructure.external.dummyjson.DummyJsonClient;
import com.example.hobby_wishlist.domain.ExternalProduct;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class DummyJsonClientUnitTests {
    private DummyJsonClient dummyJsonClient;

    @BeforeEach
    void setUp() {
        dummyJsonClient = new DummyJsonClient();
    }

    @Test
    @DisplayName("DummyJson에서 정상적으로 가져오는지 테스트")
    void verifyDummyJsonTest() {
        List<ExternalProduct> productList = dummyJsonClient.search("phone", 10, 0);
        assertNotNull(productList);
        if (!productList.isEmpty()) {
            assertTrue(productList.stream().allMatch(Objects::nonNull));
        }
    }
}
