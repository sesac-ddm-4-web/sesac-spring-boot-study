package com.example.personal_project.application;

import org.springframework.stereotype.Service;

@Service
public class ValidationService {
    public String validateName(String name) {
        if (name == null) {
            throw new IllegalArgumentException("이름을 입력해주세요.");
        }
        name = name.strip();

        if (name.isEmpty()) {
            throw new IllegalArgumentException("이름을 입력해주세요.");
        }
        return name;
    }

    public int validatePrice(Integer price) {
        if (price == null || price < 0) {
        throw new IllegalArgumentException("가격은 0원 이상이어야 합니다.");}
        return price;
    }

    public int validateBudget(int budget) {
        if (budget < 0) {
            throw new IllegalArgumentException("예산은 0원 이상이어야 합니다.");
        } return budget;
    }
}
