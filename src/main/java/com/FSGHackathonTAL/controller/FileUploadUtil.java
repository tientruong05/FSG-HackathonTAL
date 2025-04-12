package com.FSGHackathonTAL.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Lớp tiện ích để xử lý việc tải lên và lưu trữ file.
 * Cấu hình đường dẫn lưu trữ từ properties và cung cấp phương thức để lưu file
 * với tên ngẫu nhiên (UUID) nhưng giữ nguyên phần mở rộng.
 */
@Component
public class FileUploadUtil {
    
    private static final Logger logger = LoggerFactory.getLogger(FileUploadUtil.class);

    // Đường dẫn thư mục lưu trữ file, luôn sử dụng thư mục uploads trong static
    @Value("${file.upload-dir:#{null}}")
    private String configUploadDir;
    
    // Dùng để tải lại resource sau khi lưu, đảm bảo tính nhất quán
    private final ResourceLoader resourceLoader;
    
    /**
     * Constructor để inject ResourceLoader.
     * @param resourceLoader ResourceLoader được inject bởi Spring.
     */
    public FileUploadUtil(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    /**
     * Lưu một MultipartFile vào thư mục uploads.
     * Tạo tên file ngẫu nhiên bằng UUID và giữ nguyên phần mở rộng gốc.
     * Tạo thư mục nếu chưa tồn tại.
     * Ghi đè file nếu tên file đã tồn tại (do UUID gần như không trùng).
     * Trả về đường dẫn tương đối của file đã lưu (ví dụ: /uploads/uuid.jpg).
     *
     * @param file MultipartFile cần lưu.
     * @return Đường dẫn tương đối của file đã lưu.
     * @throws IOException Nếu file rỗng hoặc có lỗi I/O trong quá trình lưu.
     */
    public String saveFile(MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new IOException("Không thể lưu file rỗng.");
        }
        
        // Xác định đường dẫn uploads thực tế
        Path uploadPath;
        String projectDir = System.getProperty("user.dir");
        
        // Đầu tiên kiểm tra cấu hình từ application.properties
        if (configUploadDir != null && !configUploadDir.isEmpty()) {
            uploadPath = Paths.get(configUploadDir);
        } else {
            // Sử dụng đường dẫn mặc định trong project
            uploadPath = Paths.get(projectDir, "src", "main", "resources", "static", "uploads");
            
            // Cũng kiểm tra trường hợp đường dẫn với FSGHackathonTAL
            Path altPath = Paths.get(projectDir, "FSGHackathonTAL", "src", "main", "resources", "static", "uploads");
            if (Files.exists(altPath)) {
                uploadPath = altPath;
            }
        }
        
        logger.info("Đường dẫn upload: {}", uploadPath);

        // Tạo thư mục nếu chưa tồn tại
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
            logger.info("Đã tạo thư mục upload: {}", uploadPath);
        }

        // Tạo tên file ngẫu nhiên (UUID) để tránh trùng lặp
        String randomFileName = UUID.randomUUID().toString();
        
        // Lấy phần mở rộng của file gốc (ví dụ: .jpg, .png)
        String originalFileName = file.getOriginalFilename();
        String fileExtension = "";
        if (originalFileName != null && originalFileName.contains(".")) {
            fileExtension = originalFileName.substring(originalFileName.lastIndexOf("."));
        }

        // Kết hợp UUID và phần mở rộng thành tên file mới
        String newFileName = randomFileName + fileExtension;
        Path filePath = uploadPath.resolve(newFileName);
        
        logger.info("Đang lưu file tới: {}", filePath);

        // Sao chép nội dung file vào đường dẫn đích, ghi đè nếu tồn tại
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
        
        // Cố gắng làm mới resource để hệ thống nhận diện file mới ngay lập tức
        try {
            resourceLoader.getResource("file:" + filePath).getInputStream().close();
        } catch (Exception e) {
            logger.warn("Cảnh báo khi làm mới resource: {}", e.getMessage());
        }
        
        // Trả về đường dẫn tương đối để sử dụng trong HTML/template
        return "/uploads/" + newFileName;
    }
}
