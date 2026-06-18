package com.example.learningVocabularyPlatform.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Value("${app.media.root:./mediaFull}")
    private String mediaRoot;
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
            .allowedOrigins("http://localhost:5170", "http://localhost:5173", "http://localhost:3000")
            .allowedMethods("*")
            .allowedHeaders("*")
            .allowCredentials(true)
            .maxAge(3600);
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String fileLocation = mediaRoot;
        if (!fileLocation.endsWith("/")) {
            fileLocation = fileLocation + "/";
        }
        registry.addResourceHandler("/mediaFull/**")
            .addResourceLocations(
                "classpath:/mediaFull/",
                "file:./mediaFull/",
                "file:../mediaFull/",
                "file:" + fileLocation
            );
    }
}
