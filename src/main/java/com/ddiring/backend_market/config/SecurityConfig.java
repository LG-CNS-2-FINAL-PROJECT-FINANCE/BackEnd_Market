package com.ddiring.backend_market.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer; // CSRF 비활성화를 위해 추가

@Configuration
@EnableWebSecurity // Spring Security 설정을 활성화합니다.
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // 1. CSRF 보호 비활성화 (Stateless API에서는 보통 비활성화합니다)
            .csrf(AbstractHttpConfigurer::disable)

            // 2. 세션 관리 정책을 STATELESS로 설정 (토큰 기반 인증이므로 세션을 사용하지 않음)
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

            // 3. HTTP 요청에 대한 인가 규칙 설정
            .authorizeHttpRequests(authorize -> authorize
                // ✅ 이 경로는 인증 없이 누구나 접근 가능하도록 허용
                .requestMatchers(
                    "/api/asset/account/deposit", 
                    "/api/user/auth/**", 
                    "/error" // 에러 페이지는 보통 허용해주는 것이 좋습니다
                ).permitAll()

                // 👤 그 외 "/api/user/**" 경로는 USER 또는 ADMIN 역할이 필요
                .requestMatchers("/api/user/**").hasAnyRole("USER", "ADMIN")
                
                // 👑 "/api/admin/**" 경로는 ADMIN 역할만 가능
                .requestMatchers("/api/admin/**").hasRole("ADMIN")

                // 🔒 위에서 설정한 경로 외의 모든 요청은 인증이 필요함
                .anyRequest().authenticated()
            );
            
        // 4. 여기에 나중에 만들 커스텀 JWT 인증 필터를 추가하게 됩니다.
        // .addFilterBefore(new JwtAuthenticationFilter(...), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}