package com.FSGHackathonTAL.controller;

import com.FSGHackathonTAL.entity.User;
import com.FSGHackathonTAL.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Controller quản lý các yêu cầu liên quan đến trang thông tin cá nhân (profile) của người dùng.
 * Bao gồm hiển thị trang profile và xử lý cập nhật thông tin cá nhân, mật khẩu.
 */
@Controller
public class ProfileController {

    @Autowired
    private UserService userService;
    
    @Autowired
    private FileUploadUtil fileUploadUtil;

    /**
     * Hiển thị trang thông tin cá nhân của người dùng.
     * Lấy thông tin người dùng từ session và hiển thị.
     * @param model Model để thêm thuộc tính vào view.
     * @param session HttpSession để lấy thông tin người dùng đăng nhập.
     * @return Tên view "profile" hoặc chuyển hướng về trang chủ nếu chưa đăng nhập.
     */
    @GetMapping("/profile")
    public String profilePage(Model model, HttpSession session) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        
        if (loggedInUser == null) {
            return "redirect:/home";
        }
        
        model.addAttribute("loggedInUser", loggedInUser);
        model.addAttribute("passwordError", session.getAttribute("passwordError"));
        session.removeAttribute("passwordError");
        return "profile";
    }
    
    /**
     * Xử lý yêu cầu cập nhật thông tin cá nhân (họ tên, số điện thoại, ảnh đại diện).
     * @param fullName Họ tên mới.
     * @param phone Số điện thoại mới.
     * @param image File ảnh đại diện mới (không bắt buộc).
     * @param session HttpSession để lấy thông tin người dùng.
     * @param redirectAttributes Để gửi thông báo thành công hoặc lỗi.
     * @return Chuyển hướng về trang profile.
     */
    @PostMapping("/user/update-profile")
    public String updateProfile(
            @RequestParam("fullName") String fullName,
            @RequestParam("phone") String phone,
            @RequestPart(value = "image", required = false) MultipartFile image,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        
        if (loggedInUser == null) {
            return "redirect:/home";
        }
        
        try {
            // Validate user info without checking email (email can't be changed)
            userService.validateUserInfo(fullName, loggedInUser.getEmail(), null, phone, loggedInUser.getUserId());
            
            loggedInUser.setFullName(fullName);
            loggedInUser.setPhone(phone);
            
            // Handle image upload if provided
            if (image != null && !image.isEmpty()) {
                try {
                    String imagePath = fileUploadUtil.saveFile(image);
                    loggedInUser.setImage(imagePath);
                } catch (Exception e) {
                    redirectAttributes.addFlashAttribute("errorMessage", "Lỗi khi tải lên hình ảnh: " + e.getMessage());
                }
            }
            
            userService.saveUser(loggedInUser);
            redirectAttributes.addFlashAttribute("successMessage", "Thông tin cá nhân đã được cập nhật thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi khi cập nhật thông tin: " + e.getMessage());
        }
        
        return "redirect:/profile";
    }
    
    /**
     * Xử lý yêu cầu cập nhật mật khẩu.
     * Kiểm tra mật khẩu hiện tại, xác thực mật khẩu mới và xác nhận mật khẩu.
     * @param currentPassword Mật khẩu hiện tại.
     * @param newPassword Mật khẩu mới.
     * @param confirmPassword Xác nhận mật khẩu mới.
     * @param session HttpSession để lấy thông tin người dùng.
     * @param redirectAttributes Để gửi thông báo thành công hoặc lỗi (bao gồm lỗi mật khẩu).
     * @return Chuyển hướng về trang profile.
     */
    @PostMapping("/user/update-password")
    public String updatePassword(
            @RequestParam("currentPassword") String currentPassword,
            @RequestParam("newPassword") String newPassword,
            @RequestParam("confirmPassword") String confirmPassword,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        
        if (loggedInUser == null) {
            return "redirect:/home";
        }
        
        try {
            // Verify current password
            if (!loggedInUser.getPassword().equals(currentPassword)) {
                redirectAttributes.addFlashAttribute("passwordError", "Mật khẩu hiện tại không đúng");
                return "redirect:/profile";
            }
            
            // Check if new password meets requirements
            if (!userService.isValidPassword(newPassword)) {
                redirectAttributes.addFlashAttribute("passwordError", "Mật khẩu mới phải có ít nhất 8 ký tự và không chứa khoảng trắng");
                return "redirect:/profile";
            }
            
            // Verify password confirmation
            if (!newPassword.equals(confirmPassword)) {
                redirectAttributes.addFlashAttribute("passwordError", "Mật khẩu xác nhận không khớp");
                return "redirect:/profile";
            }
            
            // Update password
            loggedInUser.setPassword(newPassword);
            userService.saveUser(loggedInUser);
            redirectAttributes.addFlashAttribute("successMessage", "Mật khẩu đã được cập nhật thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("passwordError", "Lỗi khi cập nhật mật khẩu: " + e.getMessage());
        }
        
        return "redirect:/profile";
    }
}
