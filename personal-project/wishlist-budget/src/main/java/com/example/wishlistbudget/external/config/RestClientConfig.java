package com.example.wishlistbudget.external.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.net.http.HttpClient;
import java.time.Duration;

@Configuration
public class RestClientConfig {

    @Bean("DummyJsonRestClient")
    public RestClient dummyJsonRestClient(RestClient.Builder builder) {

        // 연결 시간 제한 3초 설정
        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(3))
                .build();

        // 응답 읽기 시간 제한 5초 설정
        JdkClientHttpRequestFactory factory =
                new JdkClientHttpRequestFactory(httpClient);
        factory.setReadTimeout(Duration.ofSeconds(5));


        // DummyJSON 서버에 요청할 RestClient 생성
        return   builder
                .baseUrl("https://dummyjson.com")
                .requestFactory(factory)
                .build();
    }
}
