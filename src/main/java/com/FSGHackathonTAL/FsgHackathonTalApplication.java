package com.FSGHackathonTAL;

import java.io.File;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.util.ResourceUtils;
import org.springframework.core.io.ClassPathResource;

import java.nio.file.Paths;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SpringBootApplication
public class FsgHackathonTalApplication {
	
	private static final Logger logger = LoggerFactory.getLogger(FsgHackathonTalApplication.class);

	public static void main(String[] args) {
		SpringApplication.run(FsgHackathonTalApplication.class, args);
	}

	@Bean
	public WebMvcConfigurer webMvcConfigurer() {
		return new WebMvcConfigurer() {
			@Override
			public void addResourceHandlers(ResourceHandlerRegistry registry) {
				try {
					// Cấu hình chỉ cho music và sounds - uploads được xử lý trong WebMvcConfig
					// để tránh xung đột mapping
					
					// Cấu hình cho thư mục music
					String projectDir = System.getProperty("user.dir");
					String musicPath = Paths.get(projectDir, "src", "main", "resources", "static", "music").toString();
					File musicDir = new File(musicPath);
					if (!musicDir.exists()) {
						logger.info("Thư mục music không tồn tại, đang tạo: {}", musicPath);
						musicDir.mkdirs();
					}
					String musicFileUrl = String.format("file:%s/", musicDir.getAbsolutePath().replace("\\", "/"));
					logger.info("Cấu hình ResourceHandler cho music: {}", musicFileUrl);
					registry.addResourceHandler("/music/**")
							.addResourceLocations(musicFileUrl, "classpath:/static/music/")
							.setCachePeriod(0);
					
					// Cấu hình cho thư mục sounds
					String soundsPath = Paths.get(projectDir, "src", "main", "resources", "static", "sounds").toString();
					File soundsDir = new File(soundsPath);
					if (!soundsDir.exists()) {
						logger.info("Thư mục sounds không tồn tại, đang tạo: {}", soundsPath);
						soundsDir.mkdirs();
					}
					String soundsFileUrl = String.format("file:%s/", soundsDir.getAbsolutePath().replace("\\", "/"));
					logger.info("Cấu hình ResourceHandler cho sounds: {}", soundsFileUrl);
					registry.addResourceHandler("/sounds/**")
							.addResourceLocations(soundsFileUrl, "classpath:/static/sounds/")
							.setCachePeriod(0);
							
				} catch (Exception e) {
					logger.error("Lỗi khi cấu hình thư mục tĩnh: {}", e.getMessage(), e);
				}
			}
		};
	}
	
	@Bean
	public RestTemplate restTemplate() {
		return new RestTemplate();
	}
	
}
