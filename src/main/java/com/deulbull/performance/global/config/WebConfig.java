package com.deulbull.performance.global.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins(
                        // 사용자 페이지
                        "http://localhost:3000",

                        // "https://deulbull.netlify.app",
                        "https://bal-ak.netlify.app"
                        
                        // 관리자 페이지
                        // "http://localhost:3002",
                        // "https://deulbull-admin.netlify.app"
                )
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                .allowCredentials(true);
    }
}