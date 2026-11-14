package com.example.personal_project;

import com.example.personal_project.domain.repository.CollectionItemRepository;
import com.example.personal_project.domain.repository.ShoppingCartRepository;
import com.example.personal_project.infrastructure.ListCollectionItemRepository;
import com.example.personal_project.infrastructure.ListShoppingCartRepository;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

// 운영 DB 대신 컨텍스트마다 새 메모리 저장소를 사용한다.
@TestConfiguration(proxyBeanMethods = false)
class InMemoryRepositoryTestConfig {
    @Bean("databaseCollectionItemRepository")
    @Primary
    CollectionItemRepository testItems() {
        return new ListCollectionItemRepository();
    }

    @Bean("databaseShoppingCartRepository")
    @Primary
    ShoppingCartRepository testCarts() {
        return new ListShoppingCartRepository();
    }
}
