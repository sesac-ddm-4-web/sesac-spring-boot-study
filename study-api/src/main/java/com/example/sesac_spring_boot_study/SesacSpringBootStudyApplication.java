package com.example.sesac_spring_boot_study;

import com.example.sesac_spring_boot_study.controller.ProductController;
import com.example.sesac_spring_boot_study.service.ProductService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class SesacSpringBootStudyApplication {

	public static void main(String[] args) {

//		ProductController productController = new ProductController(new ProductService());
//		productController.hello("대호");
		SpringApplication.run(SesacSpringBootStudyApplication.class, args);
	}

}
