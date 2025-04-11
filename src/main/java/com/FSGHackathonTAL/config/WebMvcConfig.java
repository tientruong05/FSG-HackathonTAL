package com.FSGHackathonTAL.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.CacheControl;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.concurrent.TimeUnit;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Static resources with long expiry times (1 month)
        registry.addResourceHandler("/css/**", "/js/**", "/images/**", "/webjars/**")
                .addResourceLocations("classpath:/static/css/", "classpath:/static/js/", 
                                      "classpath:/static/images/", "classpath:/META-INF/resources/webjars/")
                .setCacheControl(CacheControl.maxAge(30, TimeUnit.DAYS).cachePublic());
        
        // User uploads with shorter expiry (1 day)
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("classpath:/static/uploads/")
                .setCacheControl(CacheControl.maxAge(1, TimeUnit.DAYS).cachePublic());
                
        // No caching for dynamic content
        registry.addResourceHandler("/dynamic/**")
                .addResourceLocations("classpath:/static/dynamic/")
                .setCacheControl(CacheControl.noCache().mustRevalidate());
    }
}
