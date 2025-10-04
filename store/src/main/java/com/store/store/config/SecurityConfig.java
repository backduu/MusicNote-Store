package com.store.store.config;

import com.store.store.component.JwtAuthenticationFilter;
import com.store.store.service.impl.UserDetailsServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Autowired
    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, UserDetailsService userDetailsService, PasswordEncoder passwordEncoder) throws Exception {
        http.httpBasic(AbstractHttpConfigurer::disable) // basic authentication filter 비활성화
                .cors(Customizer.withDefaults())
                .csrf(AbstractHttpConfigurer::disable) // csrf 비활성화
                // 세션을 사용하지 않는 Stateless 방식으로 설정
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Spring Security의 필터체인은 순차적으로 검사하므로 아래 순서를 지켜야 함.
                        // 1. 인증 없이 접근 가능한 경로
                        .requestMatchers("/api/auth/login","/api/users/signup").permitAll()
                        .requestMatchers(
                                "/swagger-ui.html",
                                "/swagger-ui/**",
                                "/v3/api-docs/**"
                        ).permitAll()
                        .requestMatchers(PathRequest.toStaticResources().atCommonLocations()).permitAll()
                        // 2. 나머지 /api/users/**, /api/chatrooms/**는 인증 필요
                        .requestMatchers("/api/users/**").permitAll()
                        // 3. 관리자 전용
                        .requestMatchers("/api/admin/**").hasAnyRole("ADMIN")
                        // 에러 허용
                        .requestMatchers("/error").permitAll()
                        // 5. 그 외 모든 요청 인증 필요
                        .anyRequest().authenticated()
                )
                .formLogin(AbstractHttpConfigurer::disable)
                .logout(AbstractHttpConfigurer::disable)
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                // spring security6 부턴 람다 스타일로 authenticationManager 설정
                .authenticationProvider(authenticationProvider(userDetailsService, passwordEncoder));

        return http.build();
    }

    // (프론트엔드 개발 서버: http://localhost:5173)에서의 요청을 허용
    // 허용 메소드 : GET, POST, PUT, DELETE, PATCH, OPTIONS
    // * - 허용 헤더: 모든 헤더("*")
    // * - 자격 증명(쿠키, 인증 헤더 등) 포함 요청 허용
    // * - 모든 경로("/**")에 대해 위 설정 적용
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("http://localhost:5173"));
        //테스트용
        //configuration.setAllowedOrigins(List.of("*")); // 모든 Origin 허용
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    // Spring security6 부터는 DaoAuthenticationProvider#setUserDetailsService(...) 메서드가 Deprecated되어 생성자에서 바로 주입받는 방식으로 바뀜.
    @Bean
    public DaoAuthenticationProvider authenticationProvider(UserDetailsService userDetailsService, PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder);
        return authProvider;
    }
}
