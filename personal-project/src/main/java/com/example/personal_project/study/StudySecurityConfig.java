package com.example.personal_project.study;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.csrf.CsrfException;
import tools.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

@Configuration
public class StudySecurityConfig {
    @Bean PasswordEncoder studyPasswordEncoder() { return new BCryptPasswordEncoder(); }
    @Bean UserDetailsService studyUsers(StudyStore store) {
        return login -> {
            var m=store.memberByLogin(login);
            if(m==null) throw new UsernameNotFoundException("계정을 찾을 수 없습니다.");
            return new StudyPrincipal(m.id(),m.loginId(),m.passwordHash(),m.displayName(),m.role());
        };
    }
    @Bean SecurityFilterChain studySecurity(HttpSecurity http,ObjectMapper json) throws Exception {
        http.authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/csrf","/api/auth/login").permitAll()
                .requestMatchers("/api/auth/me","/api/auth/logout").authenticated()
                .requestMatchers(org.springframework.http.HttpMethod.GET,"/api/sessions/**","/api/materials/**","/study/**").permitAll()
                .requestMatchers("/api/presenters").hasRole("ADMIN")
                .requestMatchers("/api/my/**").hasRole("PRESENTER")
                .requestMatchers(org.springframework.http.HttpMethod.POST,"/api/sessions/*/materials").hasRole("PRESENTER")
                .requestMatchers(org.springframework.http.HttpMethod.PUT,"/api/materials/*").hasRole("PRESENTER")
                .requestMatchers("/api/sessions/**").hasRole("ADMIN")
                .requestMatchers("/shortenUrl","/shortenUrl/**").denyAll()
                .requestMatchers("/api/**").denyAll()
                .anyRequest().permitAll());
        // 기존 상품 실습 API는 유지하며, 세션 인증을 사용하는 스터디 API의 변경 요청을 보호한다.
        http.csrf(csrf -> csrf.requireCsrfProtectionMatcher(request ->
                request.getRequestURI().startsWith(request.getContextPath()+"/api/")
                && !java.util.Set.of("GET","HEAD","OPTIONS","TRACE").contains(request.getMethod())));
        http.formLogin(login -> login.loginProcessingUrl("/api/auth/login")
                .successHandler((request,response,authentication) -> {
                    response.setContentType("application/json;charset=UTF-8");
                    json.writeValue(response.getWriter(),((StudyPrincipal)authentication.getPrincipal()).publicInfo());
                })
                .failureHandler((request,response,e) -> error(response,json,401,"UNAUTHORIZED","로그인 정보를 확인해주세요.")));
        http.logout(logout -> logout.logoutUrl("/api/auth/logout")
                .logoutSuccessHandler((request,response,auth) -> {
                    if(auth==null || !(auth.getPrincipal() instanceof StudyPrincipal))
                        error(response,json,401,"UNAUTHORIZED","로그인이 필요합니다.");
                    else response.setStatus(204);
                }));
        http.exceptionHandling(errors -> errors
                .authenticationEntryPoint((request,response,e) -> error(response,json,401,"UNAUTHORIZED","로그인이 필요합니다."))
                .accessDeniedHandler((request,response,e) -> error(response,json,403,
                        e instanceof CsrfException?"INVALID_CSRF_TOKEN":"FORBIDDEN",
                        e instanceof CsrfException?"화면을 새로고침한 후 다시 시도해주세요.":"접근 권한이 없습니다.")));
        return http.build();
    }
    private static void error(HttpServletResponse response,ObjectMapper json,int status,String code,String message) throws IOException {
        response.setStatus(status); response.setContentType("application/json;charset=UTF-8");
        json.writeValue(response.getWriter(),Map.of("code",code,"message",message));
    }
}
