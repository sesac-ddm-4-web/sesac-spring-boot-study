package com.example.personal_project.infrastructure.config;

import com.example.personal_project.domain.product.model.PriceConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Scope;
import org.springframework.web.client.RestClient;

@Configuration
public class ProductConfig {

    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    @ConditionalOnMissingBean(RestClient.Builder.class)
    public RestClient.Builder restClientBuilder() {
        return RestClient.builder();
    }

    @Bean
    public PriceConverter priceConverter() {
        return new PriceConverter();
    }
}
