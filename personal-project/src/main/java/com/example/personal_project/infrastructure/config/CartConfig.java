package com.example.personal_project.infrastructure.config;

import com.example.personal_project.domain.cart.model.CartAmountCalculator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CartConfig {
    @Bean
    public CartAmountCalculator cartAmountCalculator() {
        return new CartAmountCalculator();
    }
}
