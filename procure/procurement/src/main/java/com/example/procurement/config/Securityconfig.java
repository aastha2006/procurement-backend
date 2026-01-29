package com.example.procurement.config;

import org.springframework.http.HttpMethod;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import com.example.procurement.filters.BasicAuthGenerator;
import com.example.procurement.filters.JwtAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class Securityconfig {

    @Bean
    public JwtAuthenticationFilter jwtFilter(com.example.procurement.filters.JwtUtils jwtUtils) {
        return new JwtAuthenticationFilter(jwtUtils);
    }

    @Bean
    public BasicAuthGenerator basicAuthFilter(PasswordEncoder passwordEncoder) {
        return new BasicAuthGenerator(passwordEncoder);
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, JwtAuthenticationFilter jwtFilter) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authz -> authz

                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // Allow unauthenticated access to /login and /register
                        .requestMatchers(
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/swagger-ui.html")
                        .permitAll()
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/api/vendors/**").permitAll()
                        .requestMatchers("/api/members/**").permitAll()

                        .requestMatchers("/api/PurchaseOrder/SapPo").hasRole("USER") // Basic auth only
                        // Require authentication for all other /myapp/v5/** endpoints
                        .requestMatchers("/api/master/**").authenticated()
                        .requestMatchers("/api/procurement/**").authenticated()
                        .requestMatchers("/api/roles/**").authenticated()
                        .requestMatchers("/api/Master/**").authenticated()

                        .anyRequest().authenticated())

                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration)
            throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public org.springframework.web.cors.CorsConfigurationSource corsConfigurationSource() {
        org.springframework.web.cors.CorsConfiguration configuration = new org.springframework.web.cors.CorsConfiguration();
        configuration.setAllowedOriginPatterns(java.util.Collections.singletonList("*"));
        configuration.setAllowedMethods(java.util.Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        configuration.setAllowedHeaders(java.util.Collections.singletonList("*"));
        configuration.setAllowCredentials(true);
        org.springframework.web.cors.UrlBasedCorsConfigurationSource source = new org.springframework.web.cors.UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
