package com.FSGHackathonTAL.controller;

import com.FSGHackathonTAL.entity.Article;
import com.FSGHackathonTAL.entity.Role;
import com.FSGHackathonTAL.entity.User;
import com.FSGHackathonTAL.service.ArticleService;
import com.FSGHackathonTAL.service.RoleService;
import com.FSGHackathonTAL.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.List;

/**
 * Controller xử lý các yêu cầu liên quan đến chức năng quản trị (admin).
 * Bao gồm quản lý người dùng (bác sĩ, người dùng thông thường) và quản lý bài viết.
 * Yêu cầu quyền admin để truy cập các endpoint trong controller này.
 */
@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private ArticleService articleService;

    @Autowired
    private UserService userService;

    @Autowired
    private RoleService roleService;

    @Autowired
    private FileUploadUtil fileUploadUtil;

    /**
     * Hiển thị trang dashboard quản trị.
     * Tải danh sách bác sĩ, người dùng, và bài viết theo phân trang.
     *
     * @param page Số trang hiện tại (mặc định là 0).
     * @param size Kích thước trang (mặc định là 10).
     * @param tab Tab đang được chọn (mặc định là "articles").
     * @param model Model để truyền dữ liệu tới view.
     * @param session HttpSession để kiểm tra thông tin người dùng đăng nhập.
     * @return Tên view "admin-dashboard" hoặc redirect về trang chủ nếu không phải admin.
     */
    @GetMapping("/dashboard")
    public String showAdminDashboard(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "articles") String tab,
            Model model,
            HttpSession session) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null || !"admin".equals(loggedInUser.getRole().getRoleName())) {
            return "redirect:/home";
        }

        try {
            // Xác thực tham số
            page = Math.max(0, page);
            size = Math.max(1, Math.min(size, 50)); // Giới hạn kích thước trang tối đa là 50
            
            Pageable pageable = PageRequest.of(page, size);
            
            // Tải dữ liệu một cách an toàn với try-catch cho mỗi trang
            Page<User> doctorPage;
            try {
                doctorPage = userService.getUsersByRolePaged("doctor", pageable);
                model.addAttribute("doctors", doctorPage.getContent());
                model.addAttribute("doctorPage", doctorPage);
            } catch (Exception e) {
                model.addAttribute("doctors", List.of());
                model.addAttribute("doctorPage", Page.empty());
                model.addAttribute("doctorError", "Lỗi khi tải danh sách bác sĩ: " + e.getMessage());
            }
            
            Page<User> userPage;
            try {
                userPage = userService.getUsersByRolePaged("user", pageable);
                model.addAttribute("users", userPage.getContent());
                model.addAttribute("userPage", userPage);
            } catch (Exception e) {
                model.addAttribute("users", List.of());
                model.addAttribute("userPage", Page.empty());
                model.addAttribute("userError", "Lỗi khi tải danh sách người dùng: " + e.getMessage());
            }
            
            Page<Article> articlePage;
            try {
                articlePage = articleService.getAllArticlesPaged(pageable);
                model.addAttribute("articles", articlePage.getContent());
                model.addAttribute("articlePage", articlePage);
            } catch (Exception e) {
                model.addAttribute("articles", List.of());
                model.addAttribute("articlePage", Page.empty());
                model.addAttribute("articleError", "Lỗi khi tải danh sách bài viết: " + e.getMessage());
            }
            
            model.addAttribute("loggedInUser", loggedInUser);
            model.addAttribute("currentTab", tab); // Truyền tab hiện tại
            return "admin-dashboard";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Đã xảy ra lỗi khi tải trang dashboard: " + e.getMessage());
            // Thêm danh sách rỗng để tránh lỗi template
            model.addAttribute("doctors", List.of());
            model.addAttribute("users", List.of());
            model.addAttribute("articles", List.of());
            model.addAttribute("doctorPage", Page.empty());
            model.addAttribute("userPage", Page.empty());
            model.addAttribute("articlePage", Page.empty());
            model.addAttribute("loggedInUser", loggedInUser);
            model.addAttribute("currentTab", tab); 
            return "admin-dashboard";
        }
    }

    // --- Quản lý Bác sĩ --- 

    /**
     * Xử lý thêm bác sĩ mới.
     *
     * @param fullName Họ tên bác sĩ.
     * @param email Email đăng nhập.
     * @param password Mật khẩu.
     * @param phone Số điện thoại.
     * @param image File ảnh đại diện (không bắt buộc).
     * @param isOnline Trạng thái trực tuyến (mặc định false).
     * @param isActive Trạng thái hoạt động (mặc định true).
     * @param session HttpSession.
     * @param redirectAttributes Dùng để gửi thông báo flash.
     * @return Redirect về trang dashboard admin, tab bác sĩ.
     */
    @PostMapping("/doctors/add")
    public String addDoctor(
            @RequestParam("fullName") String fullName,
            @RequestParam("email") String email,
            @RequestParam("password") String password,
            @RequestParam("phone") String phone,
            @RequestPart(value = "image", required = false) MultipartFile image,
            @RequestParam(value = "isOnline", defaultValue = "false") Boolean isOnline,
            @RequestParam(value = "isActive", defaultValue = "true") Boolean isActive,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        User admin = (User) session.getAttribute("loggedInUser");
        if (admin == null || !"admin".equals(admin.getRole().getRoleName())) {
            return "redirect:/home";
        }

        try {
            // Validate thông tin đầu vào
            userService.validateUserInfo(fullName, email, password, phone, null);
            
            Role doctorRole = roleService.getRoleByName("doctor");
            User doctor = new User();
            doctor.setFullName(fullName);
            doctor.setEmail(email);
            doctor.setPassword(password); // Mật khẩu sẽ được mã hóa trong service
            doctor.setPhone(phone);
            doctor.setRole(doctorRole);
            doctor.setIsOnline(isOnline);
            doctor.setIsActive(isActive);
            String imagePath = handleImageUpload(image, redirectAttributes);
            // Sử dụng ảnh mặc định nếu không có ảnh nào được tải lên
            doctor.setImage(imagePath != null ? imagePath : "/uploads/default.png");
            
            userService.saveUser(doctor);
            redirectAttributes.addFlashAttribute("successMessage", "Bác sĩ đã được thêm thành công!");
        } catch (MaxUploadSizeExceededException ex) {
        	redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: Kích thước tệp không được vượt quá 10MB.");
        } catch (IllegalArgumentException e) {
        	redirectAttributes.addFlashAttribute("errorMessage", "Lỗi dữ liệu: " + e.getMessage());
        } catch (IOException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi khi xử lý ảnh đại diện.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi không xác định khi thêm bác sĩ: " + e.getMessage());
        }
        return "redirect:/admin/dashboard?tab=doctors";
    }

    /**
     * Xử lý cập nhật thông tin bác sĩ.
     *
     * @param userId ID của bác sĩ cần cập nhật.
     * @param fullName Họ tên mới.
     * @param email Email mới.
     * @param phone Số điện thoại mới.
     * @param image File ảnh đại diện mới (không bắt buộc).
     * @param isOnline Trạng thái trực tuyến mới.
     * @param isActive Trạng thái hoạt động mới.
     * @param session HttpSession.
     * @param redirectAttributes Dùng để gửi thông báo flash.
     * @return Redirect về trang dashboard admin, tab bác sĩ.
     */
    @PostMapping("/doctors/update")
    public String updateDoctor(
            @RequestParam("userId") Integer userId,
            @RequestParam("fullName") String fullName,
            @RequestParam("email") String email,
            @RequestParam("phone") String phone,
            @RequestPart(value = "image", required = false) MultipartFile image,
            @RequestParam(value = "isOnline", defaultValue = "false") Boolean isOnline,
            @RequestParam(value = "isActive", defaultValue = "true") Boolean isActive,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        User admin = (User) session.getAttribute("loggedInUser");
        if (admin == null || !"admin".equals(admin.getRole().getRoleName())) {
            return "redirect:/home";
        }

        try {
            // Validate thông tin đầu vào (không cần kiểm tra mật khẩu khi cập nhật)
            userService.validateUserInfo(fullName, email, null, phone, userId);
            
            User doctor = userService.getUserById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy bác sĩ với ID: " + userId));
            doctor.setFullName(fullName);
            doctor.setEmail(email);
            doctor.setPhone(phone);
            doctor.setIsOnline(isOnline);
            doctor.setIsActive(isActive);
            
            // Xử lý ảnh nếu có ảnh mới được tải lên
            String imagePath = handleImageUpload(image, redirectAttributes);
            if (imagePath != null) {
            	// Cân nhắc xóa ảnh cũ nếu cần thiết
                doctor.setImage(imagePath);
            }
            
            userService.saveUser(doctor); // Lưu thông tin đã cập nhật
            redirectAttributes.addFlashAttribute("successMessage", "Thông tin bác sĩ đã được cập nhật thành công!");
        } catch (MaxUploadSizeExceededException ex) {
        	redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: Kích thước tệp không được vượt quá 10MB.");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi dữ liệu: " + e.getMessage());
        } catch (IOException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi khi xử lý ảnh đại diện.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi không xác định khi cập nhật bác sĩ: " + e.getMessage());
        }
        return "redirect:/admin/dashboard?tab=doctors";
    }

    /**
     * Xử lý xóa bác sĩ.
     *
     * @param userId ID của bác sĩ cần xóa.
     * @param session HttpSession.
     * @param redirectAttributes Dùng để gửi thông báo flash.
     * @return Redirect về trang dashboard admin, tab bác sĩ.
     */
    @PostMapping("/doctors/delete/{userId}")
    public String deleteDoctor(
            @PathVariable("userId") Integer userId,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        User admin = (User) session.getAttribute("loggedInUser");
        if (admin == null || !"admin".equals(admin.getRole().getRoleName())) {
            return "redirect:/home";
        }

        try {
            userService.deleteUser(userId);
            redirectAttributes.addFlashAttribute("successMessage", "Bác sĩ đã được xóa thành công!");
        } catch (Exception e) {
            // Log lỗi chi tiết hơn ở đây nếu cần
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi khi xóa bác sĩ: " + e.getMessage());
        }
        return "redirect:/admin/dashboard?tab=doctors";
    }

    // --- Quản lý Người dùng --- 

    /**
     * Xử lý thêm người dùng mới.
     *
     * @param fullName Họ tên người dùng.
     * @param email Email đăng nhập.
     * @param password Mật khẩu.
     * @param phone Số điện thoại.
     * @param image File ảnh đại diện (không bắt buộc).
     * @param session HttpSession.
     * @param redirectAttributes Dùng để gửi thông báo flash.
     * @return Redirect về trang dashboard admin, tab người dùng.
     */
    @PostMapping("/users/add")
    public String addUser(
            @RequestParam("fullName") String fullName,
            @RequestParam("email") String email,
            @RequestParam("password") String password,
            @RequestParam("phone") String phone,
            @RequestPart(value = "image", required = false) MultipartFile image,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        User admin = (User) session.getAttribute("loggedInUser");
        if (admin == null || !"admin".equals(admin.getRole().getRoleName())) {
            return "redirect:/home";
        }

        try {
            // Validate thông tin đầu vào
            userService.validateUserInfo(fullName, email, password, phone, null);
            
            Role userRole = roleService.getRoleByName("user");
            User user = new User();
            user.setFullName(fullName);
            user.setEmail(email);
            user.setPassword(password); // Sẽ được mã hóa trong service
            user.setPhone(phone);
            user.setRole(userRole);
            user.setIsOnline(false); // Người dùng mới mặc định không online
            user.setIsActive(true); // Người dùng mới mặc định hoạt động
            String imagePath = handleImageUpload(image, redirectAttributes);
            user.setImage(imagePath != null ? imagePath : "/uploads/default.png");
            
            userService.saveUser(user);
            redirectAttributes.addFlashAttribute("successMessage", "Người dùng đã được thêm thành công!");
        } catch (MaxUploadSizeExceededException ex) {
        	redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: Kích thước tệp không được vượt quá 10MB.");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi dữ liệu: " + e.getMessage());
        } catch (IOException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi khi xử lý ảnh đại diện.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi không xác định khi thêm người dùng: " + e.getMessage());
        }
        return "redirect:/admin/dashboard?tab=users";
    }

    /**
     * Xử lý cập nhật thông tin người dùng.
     *
     * @param userId ID của người dùng cần cập nhật.
     * @param fullName Họ tên mới.
     * @param email Email mới.
     * @param phone Số điện thoại mới.
     * @param image File ảnh đại diện mới (không bắt buộc).
     * @param isOnline Trạng thái trực tuyến mới (nếu có, thường không dùng cho user).
     * @param isActive Trạng thái hoạt động mới.
     * @param session HttpSession.
     * @param redirectAttributes Dùng để gửi thông báo flash.
     * @return Redirect về trang dashboard admin, tab người dùng.
     */
    @PostMapping("/users/update")
    public String updateUser(
            @RequestParam("userId") Integer userId,
            @RequestParam("fullName") String fullName,
            @RequestParam("email") String email,
            @RequestParam("phone") String phone,
            @RequestPart(value = "image", required = false) MultipartFile image,
            @RequestParam(value = "isOnline", required = false) Boolean isOnline, // Thường không dùng cho user
            @RequestParam(value = "isActive", required = false) Boolean isActive,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        User admin = (User) session.getAttribute("loggedInUser");
        if (admin == null || !"admin".equals(admin.getRole().getRoleName())) {
            return "redirect:/home";
        }

        try {
            // Validate thông tin (không cần pass)
            userService.validateUserInfo(fullName, email, null, phone, userId);
            
            User user = userService.getUserById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng với ID: " + userId));
            user.setFullName(fullName);
            user.setEmail(email);
            user.setPhone(phone);
            // Chỉ cập nhật isActive nếu được cung cấp
            if (isActive != null) {
            	user.setIsActive(isActive);
            }
            // isOnline thường không được quản lý trực tiếp cho user ở đây
            
            String imagePath = handleImageUpload(image, redirectAttributes);
            if (imagePath != null) {
                user.setImage(imagePath);
            }
            
            userService.saveUser(user);
            redirectAttributes.addFlashAttribute("successMessage", "Thông tin người dùng đã được cập nhật thành công!");
        } catch (MaxUploadSizeExceededException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: Kích thước tệp không được vượt quá 10MB.");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi dữ liệu: " + e.getMessage());
        } catch (IOException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi khi xử lý ảnh đại diện.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi không xác định khi cập nhật người dùng: " + e.getMessage());
        }
        return "redirect:/admin/dashboard?tab=users";
    }

    /**
     * Xử lý xóa người dùng.
     *
     * @param userId ID của người dùng cần xóa.
     * @param session HttpSession.
     * @param redirectAttributes Dùng để gửi thông báo flash.
     * @return Redirect về trang dashboard admin, tab người dùng.
     */
    @PostMapping("/users/delete/{userId}")
    public String deleteUser(
            @PathVariable("userId") Integer userId,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        User admin = (User) session.getAttribute("loggedInUser");
        if (admin == null || !"admin".equals(admin.getRole().getRoleName())) {
            return "redirect:/home";
        }

        try {
            userService.deleteUser(userId);
            redirectAttributes.addFlashAttribute("successMessage", "Người dùng đã được xóa thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi khi xóa người dùng: " + e.getMessage());
        }
        return "redirect:/admin/dashboard?tab=users";
    }

    // --- Quản lý Bài viết --- 

    /**
     * Xử lý thêm bài viết mới.
     *
     * @param title Tiêu đề bài viết.
     * @param image File ảnh minh họa (bắt buộc).
     * @param content Nội dung bài viết.
     * @param session HttpSession.
     * @param redirectAttributes Dùng để gửi thông báo flash.
     * @return Redirect về trang dashboard admin, tab bài viết.
     */
    @PostMapping("/articles/add")
    public String addArticle(
            @RequestParam("title") String title,
            @RequestPart("image") MultipartFile image,
            @RequestParam("content") String content,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        User admin = (User) session.getAttribute("loggedInUser");
        if (admin == null || !"admin".equals(admin.getRole().getRoleName())) {
            return "redirect:/home";
        }

        try {
            String imagePath = handleImageUpload(image, redirectAttributes);
            if (imagePath == null) {
                // handleImageUpload đã thêm lỗi vào redirectAttributes nếu cần
                return "redirect:/admin/dashboard?tab=articles"; 
            }
            articleService.createArticle(admin, title, imagePath, content);
            redirectAttributes.addFlashAttribute("successMessage", "Bài viết đã được thêm thành công!");
        } catch (MaxUploadSizeExceededException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: Kích thước tệp không được vượt quá 10MB.");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi dữ liệu: " + e.getMessage());
        } catch (IOException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi khi xử lý ảnh bài viết.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi không xác định khi thêm bài viết: " + e.getMessage());
        }
        return "redirect:/admin/dashboard?tab=articles";
    }

    /**
     * Hiển thị form chỉnh sửa bài viết.
     *
     * @param id ID của bài viết cần chỉnh sửa.
     * @param model Model để truyền dữ liệu bài viết tới view.
     * @param session HttpSession.
     * @return Tên view "admin-edit-article" hoặc redirect nếu không phải admin hoặc không tìm thấy bài viết.
     */
    @GetMapping("/articles/edit/{id}")
    public String showEditArticleForm(@PathVariable("id") Integer id, Model model, HttpSession session) {
        User admin = (User) session.getAttribute("loggedInUser");
        if (admin == null || !"admin".equals(admin.getRole().getRoleName())) {
            return "redirect:/home";
        }
        try {
        	Article article = articleService.getArticleById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy bài viết với ID: " + id));
            model.addAttribute("article", article);
            return "admin-edit-article"; // Trả về trang chỉnh sửa riêng
        } catch (Exception e) {
        	// Redirect về dashboard với thông báo lỗi
        	RedirectAttributes redirectAttributes = new org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap();
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi khi tải bài viết để chỉnh sửa: " + e.getMessage());
            return "redirect:/admin/dashboard?tab=articles";
        }
    }

    /**
     * Xử lý cập nhật bài viết.
     *
     * @param articleId ID của bài viết cần cập nhật.
     * @param title Tiêu đề mới.
     * @param image File ảnh minh họa mới (không bắt buộc).
     * @param content Nội dung mới.
     * @param session HttpSession.
     * @param redirectAttributes Dùng để gửi thông báo flash.
     * @return Redirect về trang dashboard admin, tab bài viết.
     */
    @PostMapping("/articles/update")
    public String updateArticle(
            @RequestParam("articleId") Integer articleId,
            @RequestParam("title") String title,
            @RequestPart(value = "image-update", required = false) MultipartFile image,
            @RequestParam("content") String content,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        User admin = (User) session.getAttribute("loggedInUser");
        if (admin == null || !"admin".equals(admin.getRole().getRoleName())) {
            return "redirect:/home";
        }
        try {
            Article article = articleService.getArticleById(articleId)
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy bài viết để cập nhật."));
            
            String imagePath = handleImageUpload(image, redirectAttributes);
            // Chỉ cập nhật ảnh nếu có ảnh mới được tải lên và xử lý thành công
            // Nếu không có ảnh mới, giữ nguyên ảnh cũ (imagePath = null)
            String finalImagePath = (imagePath != null) ? imagePath : article.getImage();
            
            articleService.updateArticle(articleId, title, finalImagePath, content);
            redirectAttributes.addFlashAttribute("successMessage", "Bài viết đã được cập nhật thành công!");
        } catch (MaxUploadSizeExceededException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: Kích thước tệp không được vượt quá 10MB.");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi dữ liệu: " + e.getMessage());
        } catch (IOException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi khi xử lý ảnh bài viết.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi không xác định khi cập nhật bài viết: " + e.getMessage());
        }
        return "redirect:/admin/dashboard?tab=articles";
    }

    /**
     * Xử lý xóa bài viết.
     *
     * @param id ID của bài viết cần xóa.
     * @param session HttpSession.
     * @param redirectAttributes Dùng để gửi thông báo flash.
     * @return Redirect về trang dashboard admin, tab bài viết.
     */
    @PostMapping("/articles/delete/{id}")
    public String deleteArticle(@PathVariable("id") Integer id, HttpSession session, RedirectAttributes redirectAttributes) {
        User admin = (User) session.getAttribute("loggedInUser");
        if (admin == null || !"admin".equals(admin.getRole().getRoleName())) {
            return "redirect:/home";
        }
        try {
            articleService.deleteArticle(id);
            redirectAttributes.addFlashAttribute("successMessage", "Bài viết đã được xóa thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi khi xóa bài viết: " + e.getMessage());
        }
        return "redirect:/admin/dashboard?tab=articles";
    }
    
    /**
     * Xử lý ngoại lệ khi kích thước file upload vượt quá giới hạn.
     *
     * @param ex Ngoại lệ MaxUploadSizeExceededException.
     * @param redirectAttributes Dùng để gửi thông báo flash.
     * @return Redirect về trang dashboard admin với thông báo lỗi.
     */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public String handleMaxUploadSizeExceeded(MaxUploadSizeExceededException ex, RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: Kích thước tệp không được vượt quá 10MB.");
        // Xác định tab hiện tại hoặc redirect về tab mặc định
        // Cần cách lấy tab hiện tại hoặc mặc định (ví dụ: articles)
        // Tạm thời redirect về tab articles
        return "redirect:/admin/dashboard?tab=articles"; 
    }

    /**
     * Phương thức tiện ích xử lý việc tải lên và lưu file ảnh.
     *
     * @param image MultipartFile chứa dữ liệu ảnh.
     * @param redirectAttributes Dùng để thêm thông báo lỗi nếu có.
     * @return Đường dẫn tới file ảnh đã lưu hoặc null nếu có lỗi hoặc không có ảnh.
     * @throws IOException Nếu có lỗi I/O khi lưu file.
     */
    private String handleImageUpload(MultipartFile image, RedirectAttributes redirectAttributes) throws IOException {
        if (image != null && !image.isEmpty()) {
            // Sửa lỗi: Thêm kiểm tra kiểu MIME
            String contentType = image.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: Chỉ chấp nhận tệp ảnh (jpg, png, gif, etc.).");
                return null;
            }
            
            try {
                return fileUploadUtil.saveFile(image);
            } catch (IOException e) {
            	// Log lỗi chi tiết
            	System.err.println("Lỗi khi lưu file ảnh: " + e.getMessage());
                redirectAttributes.addFlashAttribute("errorMessage", "Đã xảy ra lỗi khi tải ảnh lên.");
                throw e; // Ném lại ngoại lệ để Controller bên ngoài xử lý nếu cần
            }
        }
        return null; // Không có ảnh mới hoặc ảnh rỗng
    }
}
