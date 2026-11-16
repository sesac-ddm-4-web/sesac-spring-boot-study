package com.example.personal_project.domain;

public interface CartRepository {
    Cart getCart(); // 저장된 장바구니를 가져오기
    void save(Cart cart); // 변경된 장바구니를 저장하기
}
