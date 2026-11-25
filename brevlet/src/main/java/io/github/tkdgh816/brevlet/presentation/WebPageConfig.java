package io.github.tkdgh816.brevlet.presentation;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 프론트 페이지 진입점 설정.
 * 단일 세그먼트 경로(/{shortenUrlKey})는 단축 URL 리디렉션이 사용하므로,
 * 정적 페이지는 /page/ 아래에 두고 루트(/) 요청을 그쪽으로 포워딩한다.
 */
@Configuration
public class WebPageConfig implements WebMvcConfigurer {
    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/").setViewName("forward:/page/index.html");
    }
}
