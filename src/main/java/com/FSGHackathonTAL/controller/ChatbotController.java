package com.FSGHackathonTAL.controller;

import com.FSGHackathonTAL.dto.ChatbotMessageDTO;
import com.FSGHackathonTAL.entity.ChatbotResponse;
import com.FSGHackathonTAL.entity.User;
import com.FSGHackathonTAL.service.ChatbotService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Controller quản lý các tương tác với chatbot tâm lý.
 * Xử lý việc hiển thị giao diện chatbot và giao tiếp với ChatbotService.
 */
@Controller
public class ChatbotController {

    @Autowired
    private ChatbotService chatbotService;
    
    // Welcome message for new chat sessions
    private static final String WELCOME_MESSAGE = "Xin chào! Tôi là chatbot tâm lý, tôi có thể giúp gì cho bạn hôm nay? " +
                                                "Hãy nhớ rằng tôi chỉ có thể trả lời các câu hỏi liên quan đến sức khỏe tâm lý.";

    /**
     * Hiển thị trang giao diện người dùng của chatbot.
     * @param model Model để thêm thuộc tính vào view.
     * @param session HttpSession để lấy thông tin người dùng đăng nhập.
     * @return Tên view "chatbot" hoặc chuyển hướng về trang chủ nếu người dùng chưa đăng nhập.
     */
    @GetMapping("/chatbot")
    public String chatbotPage(Model model, HttpSession session) {
        User loggedInUser = getLoggedInUser(session);
        
        if (loggedInUser == null) {
            return "redirect:/home";
        }
        
        model.addAttribute("loggedInUser", loggedInUser);
        model.addAttribute("pageTitle", "Trò chuyện với Chatbot Tâm lý");
        model.addAttribute("welcomeMessage", WELCOME_MESSAGE);
        
        return "chatbot";
    }
    
    /**
     * Xử lý việc gửi tin nhắn đến chatbot và nhận phản hồi.
     * @param messageDTO DTO chứa nội dung tin nhắn từ người dùng.
     * @param session HttpSession để lấy thông tin người dùng đăng nhập.
     * @return ResponseEntity chứa phản hồi từ chatbot hoặc thông báo lỗi.
     */
    @PostMapping("/api/chatbot/message")
    @ResponseBody
    public ResponseEntity<?> sendMessage(@RequestBody ChatbotMessageDTO messageDTO, HttpSession session) {
        User loggedInUser = getLoggedInUser(session);
        
        if (loggedInUser == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Vui lòng đăng nhập để sử dụng chatbot"));
        }
        
        try {
            // Set message metadata
            messageDTO.setSentAt(LocalDateTime.now());
            messageDTO.setSenderId(loggedInUser.getUserId());
            
            // Get response from the chatbot service
            String question = messageDTO.getMessageContent();
            
            // Only respond to mental health related questions
            if (!chatbotService.isMentalHealthRelated(question)) {
                return ResponseEntity.ok(Map.of(
                    "response", "Xin lỗi, tôi chỉ có thể trả lời các câu hỏi liên quan đến sức khỏe tâm lý. " +
                              "Hãy hỏi tôi về các vấn đề như lo âu, trầm cảm, stress, hoặc cách cải thiện sức khỏe tinh thần nhé!"
                ));
            }
            
            // Get response from the service
            String botResponse = chatbotService.getResponseAndSaveIfNew(question);
            
            // Create response payload
            Map<String, Object> response = new HashMap<>();
            response.put("response", botResponse);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Lỗi xử lý tin nhắn: " + e.getMessage()));
        }
    }
    
    /**
     * Phương thức trợ giúp để lấy thông tin người dùng đang đăng nhập.
     * Ưu tiên lấy từ SecurityContextHolder, nếu không có thì lấy từ HttpSession.
     * @param session HttpSession hiện tại.
     * @return Đối tượng User đang đăng nhập hoặc null nếu không tìm thấy.
     */
    private User getLoggedInUser(HttpSession session) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User loggedInUser = null;
        
        if (auth != null && auth.isAuthenticated() && auth.getPrincipal() instanceof User) {
            loggedInUser = (User) auth.getPrincipal();
        } else {
            loggedInUser = (User) session.getAttribute("loggedInUser");
        }
        
        return loggedInUser;
    }
}
