package com.cirt.osint_dashboard.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // 1. Désactivation du CSRF (nécessaire pour les API REST/Postman/Curl)
            .csrf(csrf -> csrf.disable())
            
            // 2. Configuration du CORS mise à jour pour la prod
            .cors(cors -> cors.configurationSource(request -> {
                CorsConfiguration config = new CorsConfiguration();
                config.setAllowedOrigins(List.of(
                    "http://localhost:3000", 
                    "http://localhost:3001", 
                    "https://osint-dashboard-frontend.vercel.app" // TON URL VERCEL
                ));
                config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
                config.setAllowedHeaders(List.of("*"));
                config.setAllowCredentials(true);
                return config;
            }))

            // 3. Gestion des accès
           // Modifie cette section dans SecurityConfig.java
            .authorizeHttpRequests(auth -> auth
            .requestMatchers("/auth/**").permitAll()
            .requestMatchers("/search/**").permitAll()
            .requestMatchers("/api/**").permitAll() // Couvre les routes de type /api/person/...
            .anyRequest().permitAll() 
            )
            
            // 4. On garde le Basic Auth en option pour tes tests Curl
            .httpBasic(org.springframework.security.config.Customizer.withDefaults());

        return http.build();
    }
}