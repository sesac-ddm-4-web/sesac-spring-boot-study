package com.example.personal_project;

import com.example.personal_project.domain.entity.CollectionItem;
import com.example.personal_project.domain.repository.CollectionItemRepository;
import com.example.personal_project.infrastructure.DummyJsonClient;
import com.example.personal_project.presentation.dto.collectionItem.CollectionItemRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.RestClient;

@SpringBootTest(properties = "spring.main.allow-bean-definition-overriding=true")
@AutoConfigureMockMvc
@Import({ApiIntegrationTestSupport.ExternalApiTestConfig.class, InMemoryRepositoryTestConfig.class})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
abstract class ApiIntegrationTestSupport {
    @Autowired protected MockMvc mockMvc;
    @Autowired protected CollectionItemRepository items;
    @Autowired protected ExternalApiFixture externalApi;

    protected long createItem(int price) {
        return items.add(CollectionItem.createInternalItem(
                new CollectionItemRequest("테스트 상품", price))).getId();
    }

    // 실제 외부 네트워크 대신 HTTP 요청/응답을 검증하는 서버를 연결한다.
    static class ExternalApiFixture {
        final MockRestServiceServer server;
        final DummyJsonClient client;

        ExternalApiFixture() {
            RestClient.Builder builder = RestClient.builder().baseUrl("https://dummyjson.com");
            server = MockRestServiceServer.bindTo(builder).build();
            client = new DummyJsonClient(builder.build());
        }
    }

    @TestConfiguration(proxyBeanMethods = false)
    static class ExternalApiTestConfig {
        @Bean
        ExternalApiFixture externalApiFixture() {
            return new ExternalApiFixture();
        }

        @Bean
        @Primary
        DummyJsonClient testDummyJsonClient(ExternalApiFixture fixture) {
            return fixture.client;
        }
    }
}
