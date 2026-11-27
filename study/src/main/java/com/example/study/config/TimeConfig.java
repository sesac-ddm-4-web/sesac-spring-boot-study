package com.example.study.config;

import java.time.Clock;
import java.time.ZoneId;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TimeConfig {

    /** 현재 시각의 기준. 서버의 시간대와 관계없이 한국 시간을 쓰고, 테스트에서는 교체할 수 있다. */
    @Bean
    public Clock clock() {
        return Clock.system(ZoneId.of("Asia/Seoul"));
    }
}
