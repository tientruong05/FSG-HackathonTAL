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
					// Tạo các đường dẫn đến thư mục uploads
					String projectDir = System.getProperty("user.dir");
					String uploadsPath = Paths.get(projectDir, "src", "main", "resources", "static", "uploads").toString();
					File uploadsDir = new File(uploadsPath);
					
					// Tạo thư mục nếu chưa tồn tại
					if (!uploadsDir.exists()) {
						logger.info("Thư mục uploads không tồn tại, đang tạo: {}", uploadsPath);
						uploadsDir.mkdirs();
					}
					
					// Tạo File URL phù hợp với hệ điều hành
					String fileUrl = String.format("file:%s/", uploadsDir.getAbsolutePath().replace("\\", "/"));
					
					logger.info("Cấu hình ResourceHandler cho uploads: {}", fileUrl);
					registry.addResourceHandler("/uploads/**")
							.addResourceLocations(fileUrl)
							.setCachePeriod(0);
					
					// Thêm bản sao lưu từ classpath trong trường hợp ứng dụng được đóng gói (JAR/WAR)
					registry.addResourceHandler("/uploads/**")
							.addResourceLocations("classpath:/static/uploads/")
							.setCachePeriod(0);
				} catch (Exception e) {
					logger.error("Lỗi khi cấu hình thư mục uploads: {}", e.getMessage(), e);
				}
			}
		};
	}
	
	@Bean
	public RestTemplate restTemplate() {
		return new RestTemplate();
	}
	
}
