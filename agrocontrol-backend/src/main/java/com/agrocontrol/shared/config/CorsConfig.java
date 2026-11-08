package com.agrocontrol.shared.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * CORS para DESARROLLO (Guia 05 del frontend).
 * El navegador solo deja que la pagina de Vite (http://localhost:5173) llame a esta API
 * (http://localhost:8080) si el backend lo autoriza explicitamente con este origen.
 * CORS no autentica usuarios ni protege la base de datos: solo dice que origen del
 * navegador puede leer las respuestas. En produccion se cambia por el dominio real del frontend.
 */
@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins("http://localhost:5173")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*");
    }
}
