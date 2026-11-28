package com.techstore.techstore_api.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpMethod;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .cors(Customizer.withDefaults())
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html", "/favicon.ico", "/error").permitAll()
                .requestMatchers("/uploads/**").permitAll()

                // ACCÈS PUBLIC : LOGIQUE MÉTIER
                .requestMatchers("/api/v1/auth/**").permitAll()
                .requestMatchers(
                    "/api/v1/products", "/api/v1/products/**", 
                    "/api/v1/categories", "/api/v1/categories/**", 
                    "/api/v1/collections", "/api/v1/collections/**", 
                    "/api/v1/shipping-zones", "/api/v1/shipping-zones/**", 
                    "/api/v1/orders", "/api/v1/orders/**", 
                    "/api/v1/cart", "/api/v1/cart/**"
                ).permitAll()

                // ACCÈS PUBLIC : REVIEWS, SETTINGS & ABOUT
                .requestMatchers(HttpMethod.GET, "/api/v1/reviews/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/v1/settings", "/api/v1/about").permitAll()

                // ACCÈS PRIVÉ : ADRESSES & PROFIL
                .requestMatchers("/api/v1/addresses/**").authenticated()
                .requestMatchers("/api/v1/users/me").authenticated()

                // ACCÈS PRIVÉ : ADMINISTRATION
                .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")

                // TOUT LE RESTE
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(List.of(
            "http://localhost:*",
            "http://127.0.0.1:*",
            "https://*.vercel.app",
            "https://petit-eleveur-ouest-cameroun.vercel.app",
            "https://techelectronique-frond-end.vercel.app",
            "https://techelectronique-front-end.vercel.app",
            "https://*.onrender.com",
            "*"
        ));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS", "HEAD"));
        configuration.setAllowedHeaders(List.of("*")); 
        configuration.setExposedHeaders(Arrays.asList("X-Session-Id", "Authorization"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
