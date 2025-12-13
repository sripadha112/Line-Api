package com.example.auth.config;

import com.example.auth.service.TokenBlacklistService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.OncePerRequestFilter;

import java.util.Arrays;

import java.io.IOException;
import java.util.Collections;

@Configuration
public class SecurityConfig {

    private final JwtUtil jwtUtil;
    private final TokenBlacklistService tokenBlacklistService;

    public SecurityConfig(JwtUtil jwtUtil, TokenBlacklistService tokenBlacklistService) {
        this.jwtUtil = jwtUtil;
        this.tokenBlacklistService = tokenBlacklistService;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable())
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**", "/api/register/**", "/api/registration/**", "/swagger-ui/**", "/v3/api-docs/**", "/api/user/**", "/api/doctor/**", "/api/doctors/**", "/api/notifications/**", "/api/test/**").permitAll()
                .anyRequest().authenticated()
            )
            .httpBasic(Customizer.withDefaults());

        http.addFilterBefore(new JwtFilter(jwtUtil, tokenBlacklistService), UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
    
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(Arrays.asList("*"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(false);
        configuration.setMaxAge(3600L);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    static class JwtFilter extends OncePerRequestFilter {
        private final JwtUtil jwtUtil;
        private final TokenBlacklistService tokenBlacklistService;

        public JwtFilter(JwtUtil jwtUtil, TokenBlacklistService tokenBlacklistService) { 
            this.jwtUtil = jwtUtil; 
            this.tokenBlacklistService = tokenBlacklistService;
        }

        @Override
        protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
                throws ServletException, IOException {
            
            // Skip JWT validation for auth endpoints, registration endpoints, swagger docs, and other public endpoints
            String requestPath = request.getRequestURI();
            if (requestPath.startsWith("/api/auth/") || 
                requestPath.startsWith("/api/register/") ||
                requestPath.startsWith("/api/registration/") ||
                requestPath.startsWith("/swagger-ui/") || 
                requestPath.startsWith("/v3/api-docs/") ||
                requestPath.startsWith("/api/doctors/")) {
                filterChain.doFilter(request, response);
                return;
            }
            
            String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                try {
                    // Check if token is blacklisted
                    if (tokenBlacklistService.isTokenBlacklisted(token)) {
                        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                        response.getWriter().write("{\"error\":\"Token has been invalidated\"}");
                        response.setContentType("application/json");
                        return;
                    }
                    
                    Jws<Claims> claims = jwtUtil.parseToken(token);
                    String sub = claims.getBody().getSubject();
                    UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(sub, null, Collections.emptyList());
                    SecurityContextHolder.getContext().setAuthentication(auth);
                } catch (JwtException e) {
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    return;
                }
            }
            filterChain.doFilter(request, response);
        }
    }
}
