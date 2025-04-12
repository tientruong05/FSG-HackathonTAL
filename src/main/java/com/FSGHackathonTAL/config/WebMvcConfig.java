package com.FSGHackathonTAL.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.CacheControl;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
    
    private static final Logger logger = LoggerFactory.getLogger(WebMvcConfig.class);

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Static resources with long expiry times (1 month)
        registry.addResourceHandler("/css/**", "/js/**", "/images/**", "/webjars/**")
                .addResourceLocations("classpath:/static/css/", "classpath:/static/js/", 
                                      "classpath:/static/images/", "classpath:/META-INF/resources/webjars/")
                .setCacheControl(CacheControl.maxAge(30, TimeUnit.DAYS).cachePublic());
        
        // Cấu hình cho thư mục uploads
        try {
            String projectDir = System.getProperty("user.dir");
            
            // Kiểm tra xem đang ở thư mục gốc hay thư mục con FSGHackathonTAL
            Path uploadsPath;
            Path fsghackathontalPath = Paths.get(projectDir, "FSGHackathonTAL");
            
            if (Files.exists(fsghackathontalPath) && Files.isDirectory(fsghackathontalPath)) {
                // Đường dẫn với FSGHackathonTAL là thư mục con
                uploadsPath = Paths.get(projectDir, "FSGHackathonTAL", "src", "main", "resources", "static", "uploads");
            } else {
                // Đường dẫn với FSGHackathonTAL là thư mục hiện tại
                uploadsPath = Paths.get(projectDir, "src", "main", "resources", "static", "uploads");
            }
            
            File uploadsDir = uploadsPath.toFile();
            if (!uploadsDir.exists()) {
                uploadsDir.mkdirs();
                logger.info("Đã tạo thư mục uploads: {}", uploadsPath);
            }
            
            // Đường dẫn file URL cho resource handler
            String fileUrl = String.format("file:%s/", uploadsDir.getAbsolutePath().replace("\\", "/"));
            logger.info("Cấu hình ResourceHandler cho uploads: {}", fileUrl);
            
            // Đăng ký đường dẫn /uploads/**
            registry.addResourceHandler("/uploads/**")
                    .addResourceLocations(fileUrl, "classpath:/static/uploads/");
        } catch (Exception e) {
            logger.error("Lỗi khi cấu hình thư mục uploads: {}", e.getMessage(), e);
        }
        
        // No caching for dynamic content
        registry.addResourceHandler("/dynamic/**")
                .addResourceLocations("classpath:/static/dynamic/")
                .setCacheControl(CacheControl.noCache().mustRevalidate());
                
        // Cấu hình cho thư mục music và sounds
        registry.addResourceHandler("/music/**")
                .addResourceLocations("classpath:/static/music/", "file:./src/main/resources/static/music/")
                .setCacheControl(CacheControl.maxAge(30, TimeUnit.DAYS).cachePublic());
                
        registry.addResourceHandler("/sounds/**")
                .addResourceLocations("classpath:/static/sounds/", "file:./src/main/resources/static/sounds/")
                .setCacheControl(CacheControl.maxAge(30, TimeUnit.DAYS).cachePublic());
    }
}
