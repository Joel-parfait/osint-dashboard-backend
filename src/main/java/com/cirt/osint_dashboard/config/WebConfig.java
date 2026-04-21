package com.cirt.osint_dashboard.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                // Ajoute ici l'URL exacte de ton frontend Vercel
                .allowedOrigins(
                    "http://localhost:3000", 
                    "http://localhost:3001",
                    "https://osint-dashboard-frontend.vercel.app" 
                )
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true);
    }
}