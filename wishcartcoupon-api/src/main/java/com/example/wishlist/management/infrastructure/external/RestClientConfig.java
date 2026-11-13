package com.example.wishlist.management.infrastructure.external;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.net.http.HttpClient;
import java.time.Duration;

// dummyjson에 요청을 보낼 restclient를 어떤 설정으로 만들지 스프링에 알려 주는 설정 코드
@Configuration
public class RestClientConfig {

    @Bean
    public RestClient dummyJsonRestClient() {
        HttpClient httpClient = HttpClient.newBuilder()
                // 서버와 연결을 맺는 데 기다리는 최대 시간 (단위: 초)
                .connectTimeout(Duration.ofSeconds(2))
                .build();
        JdkClientHttpRequestFactory factory = new JdkClientHttpRequestFactory(httpClient);
        // 요청을 보낸 뒤 응답을 기다리는 최대 시간
        factory.setReadTimeout(Duration.ofSeconds(1));

        return RestClient.builder()
                .baseUrl("https://dummyjson.com")
                .requestFactory(factory)
                .build();
    }
}