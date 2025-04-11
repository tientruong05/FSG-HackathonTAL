package com.FSGHackathonTAL.controller;

import com.FSGHackathonTAL.entity.ChatSession;
import com.FSGHackathonTAL.entity.User;
import com.FSGHackathonTAL.service.ChatSessionService;
import com.FSGHackathonTAL.service.UserService;
import com.FSGHackathonTAL.service.DoctorAvailabilityService; // Import DoctorAvailabilityService
import com.FSGHackathonTAL.service.ArticleService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Map;
import java.util.HashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.messaging.simp.SimpMessagingTemplate;

/**
 * Controller xử lý các yêu cầu liên quan đến xác thực người dùng (đăng nhập, đăng ký, đăng xuất),
 * hiển thị trang chủ, chọn bác sĩ và bắt đầu/kết thúc phiên chat.
 */
@Controller
@Validated
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private ChatSessionService chatSessionService;

    @Autowired
    private ArticleService articleService;

    @Autowired
    private DoctorAvailabilityService doctorAvailabilityService; // Inject DoctorAvailabilityService

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    /**
     * Xử lý đường dẫn gốc "/"
     * Chuyển hướng đến trang chủ
     * @return Chuyển hướng đến "/home"
     */
    @GetMapping("/")
    public String index() {
        return "redirect:/home";
    }

    /**
     * Hiển thị trang chủ.
     * Lấy thông tin người dùng đăng nhập, danh sách bác sĩ online, bài viết gần đây
     * và kiểm tra phiên chat đang hoạt động (nếu là user).
     * @param model Model để thêm thuộc tính vào view.
     * @param session HttpSession để lấy/lưu thông tin người dùng.
     * @return Tên view "home".
     */
    @GetMapping("/home")
    public String home(Model model, HttpSession session) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User loggedInUser = null;
        
        if (auth != null && auth.isAuthenticated() && auth.getPrincipal() instanceof User) {
            loggedInUser = (User) auth.getPrincipal();
            session.setAttribute("loggedInUser", loggedInUser);
        } else {
            // For non-authenticated users or non-User principals
            loggedInUser = (User) session.getAttribute("loggedInUser");
        }
        
        model.addAttribute("loggedInUser", loggedInUser);
        model.addAttribute("onlineDoctors", userService.getOnlineDoctors());
        
        // Lấy danh sách bài viết cho trang chủ (cho cả user đăng nhập và không đăng nhập)
        try {
            model.addAttribute("recentArticles", articleService.getRecentArticles(5));
        } catch (Exception e) {
            // Xử lý khi không thể lấy bài viết
            model.addAttribute("recentArticles", Collections.emptyList());
        }
        
        // Nếu người dùng đã đăng nhập, kiểm tra xem có phiên chat đang hoạt động không
        if (loggedInUser != null && !"doctor".equals(loggedInUser.getRole().getRoleName())) {
            Optional<ChatSession> activeChat = chatSessionService.getUserActiveChat(loggedInUser);
            if (activeChat.isPresent()) {
                ChatSession activeChatSession = activeChat.get();
                model.addAttribute("activeChatSession", activeChatSession);
                model.addAttribute("activeDoctor", activeChatSession.getDoctor());
            }
        }
        
        return "home";
    }

    /**
     * Xử lý yêu cầu đăng nhập của người dùng.
     * Xác thực thông tin, cập nhật trạng thái online, tạo Authentication cho Spring Security,
     * lưu thông tin vào session và điều hướng đến trang phù hợp với vai trò.
     * @param email Email đăng nhập.
     * @param password Mật khẩu.
     * @param session HttpSession để lưu thông tin đăng nhập và context bảo mật.
     * @param redirectAttributes Để gửi thông báo lỗi nếu đăng nhập thất bại.
     * @return Chuyển hướng đến trang dashboard (admin), trang quản lý chat (doctor) hoặc trang chủ.
     */
    @PostMapping("/user/login")
    public String login(
            @RequestParam @NotBlank(message = "Email không được để trống!") @Email(message = "Email không đúng định dạng!") String email,
            @RequestParam @NotBlank(message = "Mật khẩu không được để trống!") String password,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        try {
            // Sử dụng UserService thay vì AuthenticationManager
            User user = userService.login(email, password);
            
            // Đảm bảo các thuộc tính lazy load được tải đúng
            if (user.getRole() != null) {
                // Chỉ cần truy cập getRoleName() đã đủ để load thông tin role
                String roleName = user.getRole().getRoleName();
                
                // Tạo đối tượng Authentication cho Spring Security
                SimpleGrantedAuthority authority = new SimpleGrantedAuthority(roleName);
                
                // Tạo authentication token và đặt vào security context
                Authentication auth = new UsernamePasswordAuthenticationToken(
                    user, password, Collections.singleton(authority));
                
                SecurityContextHolder.getContext().setAuthentication(auth);
                session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, 
                    SecurityContextHolder.getContext());
                
                // Lưu user vào session cho backward compatibility
                session.setAttribute("loggedInUser", user);
                
                // Cập nhật trạng thái online
                user.setIsOnline(true);
                userService.saveUser(user);
                
                // Điều hướng theo vai trò
                if ("admin".equals(roleName)) {
                    return "redirect:/admin/dashboard";
                } else if ("doctor".equals(roleName)) {
                    return "redirect:/doctor/chats";
                }
            }
            
            return "redirect:/home";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("loginError", e.getMessage());
            return "redirect:/home";
        }
    }

    /**
     * Xử lý yêu cầu đăng ký tài khoản mới.
     * @param fullName Họ tên người dùng.
     * @param email Email đăng ký.
     * @param password Mật khẩu.
     * @param confirmPassword Mật khẩu xác nhận.
     * @param redirectAttributes Để gửi thông báo thành công hoặc lỗi.
     * @return Chuyển hướng về trang chủ với thông báo tương ứng.
     */
    @PostMapping("/user/register")
    public String register(
            @RequestParam @NotBlank(message = "Họ tên không được để trống!") String fullName,
            @RequestParam @NotBlank(message = "Email không được để trống!") @Email(message = "Email không đúng định dạng!") String email,
            @RequestParam @NotBlank(message = "Mật khẩu không được để trống!") String password,
            @RequestParam @NotBlank(message = "Mật khẩu xác nhận không được để trống!") String confirmPassword,
            RedirectAttributes redirectAttributes) {
        try {
            User user = userService.register(fullName, email, password, confirmPassword);
            redirectAttributes.addFlashAttribute("registerSuccess", "Đăng ký thành công! Vui lòng đăng nhập.");
            return "redirect:/home";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("registerError", e.getMessage());
            return "redirect:/home";
        }
    }

    /**
     * Xử lý yêu cầu đăng xuất.
     * Cập nhật trạng thái offline của người dùng, hủy session và xóa context bảo mật.
     * @param session HttpSession cần hủy.
     * @return Chuyển hướng về trang chủ.
     */
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        // Lấy thông tin người dùng trước khi xóa session
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        
        // Cập nhật trạng thái offline nếu có thông tin người dùng
        if (loggedInUser != null) {
            loggedInUser.setIsOnline(false);
            userService.saveUser(loggedInUser);
        }
        
        // Xóa session và đăng xuất Spring Security
        session.invalidate();
        SecurityContextHolder.clearContext();
        
        return "redirect:/home";
    }

    /**
     * Hiển thị modal chọn bác sĩ.
     * Lấy danh sách bác sĩ đang online và đang bận, kiểm tra phiên chat đang hoạt động của người dùng.
     * @param model Model để thêm thuộc tính vào view.
     * @param session HttpSession để lấy thông tin người dùng đăng nhập.
     * @return Fragment "fragments :: doctorSelectionModal".
     */
    @GetMapping("/select-doctor")
    public String showDoctorSelection(Model model, HttpSession session) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User loggedInUser = null;
        
        if (auth != null && auth.isAuthenticated() && auth.getPrincipal() instanceof User) {
            loggedInUser = (User) auth.getPrincipal();
        } else {
            loggedInUser = (User) session.getAttribute("loggedInUser");
        }
        
        if (loggedInUser == null) {
            return "redirect:/home";
        }

        model.addAttribute("loggedInUser", loggedInUser);
        
        // Get available and busy doctors separately
        List<User> availableDoctors = doctorAvailabilityService.getAvailableDoctors();
        List<User> busyDoctors = doctorAvailabilityService.getBusyDoctors();
        
        model.addAttribute("availableDoctors", availableDoctors);
        model.addAttribute("busyDoctors", busyDoctors);
        model.addAttribute("timestamp", LocalDateTime.now()); // Add timestamp for debugging/cache busting if needed

        // Kiểm tra xem người dùng có phiên chat nào đang hoạt động không
        Optional<ChatSession> activeChat = chatSessionService.getUserActiveChat(loggedInUser);
        if (activeChat.isPresent()) {
            ChatSession existingChat = activeChat.get();
            if (existingChat.getEndTime() == null) {
                model.addAttribute("activeChatSession", existingChat);
                model.addAttribute("activeDoctor", existingChat.getDoctor());
            }
        }
        
        return "fragments :: doctorSelectionModal";
    }

    /**
     * Bắt đầu phiên chat giữa người dùng và bác sĩ được chọn.
     * Kiểm tra trạng thái online và tình trạng bận của bác sĩ trước khi bắt đầu.
     * @param doctorId ID của bác sĩ được chọn.
     * @param model Model để thêm thuộc tính vào view.
     * @param session HttpSession để lấy thông tin người dùng đăng nhập.
     * @param redirectAttributes Để gửi thông báo lỗi nếu không thể bắt đầu chat.
     * @return Tên view "user-chat" nếu thành công, ngược lại chuyển hướng về trang chủ.
     */
    @GetMapping("/chat/{doctorId}")
    public String chatWithDoctor(@PathVariable Integer doctorId, Model model, HttpSession session, RedirectAttributes redirectAttributes) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User loggedInUser = null;
        
        if (auth != null && auth.isAuthenticated() && auth.getPrincipal() instanceof User) {
            loggedInUser = (User) auth.getPrincipal();
        } else {
            loggedInUser = (User) session.getAttribute("loggedInUser");
        }
        
        if (loggedInUser == null) {
            return "redirect:/home";
        }
    
        try {
            User doctor = userService.getUserById(doctorId)
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy bác sĩ"));
            
            // Kiểm tra xem bác sĩ có đang online không
            if (!doctor.getIsOnline()) {
                redirectAttributes.addFlashAttribute("chatError", "Bác sĩ hiện không trực tuyến. Vui lòng chọn bác sĩ khác.");
                return "redirect:/home";
            }
            
            // Kiểm tra xem bác sĩ có đang có phiên chat nào khác không
            Optional<ChatSession> activeDoctorChat = chatSessionService.getDoctorActiveChat(doctor);
            if (activeDoctorChat.isPresent() && !activeDoctorChat.get().getUser().getUserId().equals(loggedInUser.getUserId())) {
                redirectAttributes.addFlashAttribute("chatError", "Bác sĩ đang bận với phiên chat khác. Vui lòng thử lại sau hoặc chọn bác sĩ khác.");
                return "redirect:/home";
            }

            try {
                ChatSession activeChat = chatSessionService.startChat(loggedInUser, doctor);
                model.addAttribute("chatSession", activeChat);
                model.addAttribute("doctor", doctor);
                model.addAttribute("loggedInUser", loggedInUser);
                return "user-chat";
            } catch (IllegalStateException e) {
                redirectAttributes.addFlashAttribute("chatError", e.getMessage());
                return "redirect:/home";
            }
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("chatError", "Đã xảy ra lỗi: " + e.getMessage());
            return "redirect:/home";
        }
    }    

    /**
     * Kết thúc phiên chat từ phía bác sĩ và lưu ghi chú (nếu có).
     * @param sessionId ID của phiên chat cần kết thúc.
     * @param doctorNotes Ghi chú của bác sĩ về phiên chat (không bắt buộc).
     * @param session HttpSession hiện tại.
     * @return Chuyển hướng đến trang quản lý chat của bác sĩ.
     */
    @PostMapping("/chat/end/{sessionId}")
    public String endChatWithNotes(
            @PathVariable Integer sessionId,
            @RequestParam(required = false) String doctorNotes,
            HttpSession session) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User loggedInUser = null;
        
        if (auth != null && auth.isAuthenticated() && auth.getPrincipal() instanceof User) {
            loggedInUser = (User) auth.getPrincipal();
        } else {
            loggedInUser = (User) session.getAttribute("loggedInUser");
        }
        
        if (loggedInUser == null || !"doctor".equals(loggedInUser.getRole().getRoleName())) {
            return "redirect:/home";
        }

        chatSessionService.endChat(sessionId, doctorNotes != null ? doctorNotes : "");
        return "redirect:/home";
    }
    
    /**
     * Kết thúc phiên chat từ phía người dùng.
     * @param sessionId ID của phiên chat cần kết thúc.
     * @param session HttpSession hiện tại.
     * @param redirectAttributes Để gửi thông báo.
     * @return Chuyển hướng về trang chủ.
     */
    @PostMapping("/chat/user-end/{sessionId}")
    public String endChatAsUser(
            @PathVariable Integer sessionId,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User loggedInUser = null;
        
        if (auth != null && auth.isAuthenticated() && auth.getPrincipal() instanceof User) {
            loggedInUser = (User) auth.getPrincipal();
        } else {
            loggedInUser = (User) session.getAttribute("loggedInUser");
        }
        
        if (loggedInUser == null) {
            return "redirect:/home";
        }
        
        try {
            // Get the chat session
            ChatSession session1 = chatSessionService.getChatSessionById(sessionId);
            
            // Verify that this user is part of the session
            if (!session1.getUser().getUserId().equals(loggedInUser.getUserId())) {
                redirectAttributes.addFlashAttribute("chatError", "Bạn không có quyền kết thúc phiên chat này");
                return "redirect:/home";
            }
            
            // End the chat session
            chatSessionService.endChat(sessionId, "Người dùng kết thúc phiên chat");
            
            // Send WebSocket notification about session ending
            Map<String, Object> endSessionNotification = new HashMap<>();
            endSessionNotification.put("type", "END_SESSION");
            endSessionNotification.put("sessionId", sessionId);
            endSessionNotification.put("endedByUser", true);
            endSessionNotification.put("endTime", LocalDateTime.now().toString());
            messagingTemplate.convertAndSend("/topic/session-updates", endSessionNotification);
            
            redirectAttributes.addFlashAttribute("chatSuccess", "Phiên chat đã kết thúc thành công");
            return "redirect:/home";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("chatError", "Lỗi khi kết thúc phiên chat: " + e.getMessage());
            return "redirect:/home";
        }
    }

    /**
     * Lấy nội dung cập nhật cho modal chọn bác sĩ (dùng cho polling hoặc SSE).
     * @param model Model để thêm thuộc tính.
     * @param session HttpSession để lấy thông tin người dùng.
     * @return Fragment "fragments :: doctorSelectionModalContent".
     */
    @GetMapping("/doctor-selection-content")
    public String getDoctorSelectionModalContent(Model model, HttpSession session) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User loggedInUser = null;
        
        if (auth != null && auth.isAuthenticated() && auth.getPrincipal() instanceof User) {
            loggedInUser = (User) auth.getPrincipal();
        } else {
            // Fallback to session attribute if not in security context
            loggedInUser = (User) session.getAttribute("loggedInUser");
        }
        
        // If user is not logged in, return an empty fragment or an error message fragment
        if (loggedInUser == null) {
             model.addAttribute("chatError", "Vui lòng đăng nhập để chọn bác sĩ.");
             // Return a fragment that just shows the error
             return "fragments/doctors-modal :: doctorModalBodyContent"; 
        }

        model.addAttribute("loggedInUser", loggedInUser);
        
        try {
            // Get available and busy doctors separately
            List<User> availableDoctors = doctorAvailabilityService.getAvailableDoctors();
            List<User> busyDoctors = doctorAvailabilityService.getBusyDoctors();
            
            model.addAttribute("availableDoctors", availableDoctors);
            model.addAttribute("busyDoctors", busyDoctors);
            model.addAttribute("timestamp", LocalDateTime.now()); // Add timestamp for debugging/cache busting

            // Check if the user has an active chat session
            Optional<ChatSession> activeChat = chatSessionService.getUserActiveChat(loggedInUser);
            if (activeChat.isPresent()) {
                ChatSession existingChat = activeChat.get();
                // Ensure the chat is actually active (no end time)
                if (existingChat.getEndTime() == null) { 
                    model.addAttribute("activeChatSession", existingChat);
                    model.addAttribute("activeDoctor", existingChat.getDoctor());
                }
            }
        } catch (Exception e) {
             System.err.println("Error fetching doctor list for modal: " + e.getMessage());
             model.addAttribute("chatError", "Lỗi khi tải danh sách bác sĩ: " + e.getMessage());
             model.addAttribute("availableDoctors", List.of());
             model.addAttribute("busyDoctors", List.of());
        }
        
        // Return the specific fragment containing the modal body content
        return "fragments/doctors-modal :: doctorModalBodyContent";
    }

    /**
     * Gửi thông báo cập nhật danh sách bác sĩ qua WebSocket.
     * Được gọi khi trạng thái online/offline của bác sĩ thay đổi.
     */
    private void sendDoctorListUpdate() {
        // ... existing code ...
    }
}
