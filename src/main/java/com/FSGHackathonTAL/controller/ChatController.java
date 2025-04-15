package com.FSGHackathonTAL.controller;

import com.FSGHackathonTAL.dto.MessageDTO;
import com.FSGHackathonTAL.entity.ChatSession;
import com.FSGHackathonTAL.entity.User;
import com.FSGHackathonTAL.service.ChatSessionService;
import com.FSGHackathonTAL.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Map;
import java.util.HashMap;

/**
 * Controller xử lý các hoạt động liên quan đến chat real-time và quản lý phiên chat.
 * Sử dụng WebSocket (thông qua STOMP) để gửi và nhận tin nhắn,
 * và các endpoint REST để kiểm tra trạng thái, gửi tin nhắn, kết thúc chat, lấy lịch sử,
 * và các chức năng phụ trợ như "like" bác sĩ và hiển thị bác sĩ nổi bật.
 */
@Controller
public class ChatController {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private ChatSessionService chatSessionService;
    
    @Autowired
    private UserService userService;

    /**
     * Xử lý tin nhắn đến từ WebSocket client (người dùng hoặc bác sĩ).
     * Lưu tin nhắn vào cơ sở dữ liệu, cập nhật thời gian gửi, và gửi lại tin nhắn
     * tới topic WebSocket của phiên chat tương ứng.
     * Đồng thời gửi thông báo tin nhắn mới tới topic chung của các phiên chat.
     *
     * @param messageDTO DTO chứa thông tin tin nhắn.
     */
    // Endpoint nhận tin nhắn từ WebSocket và gửi tới đúng đích
    @MessageMapping("/chat.sendMessage")
    public void handleChatMessage(MessageDTO messageDTO) {
        // Luôn đặt timestamp là thời gian hiện tại của server để đảm bảo tính nhất quán
        messageDTO.setSentAt(LocalDateTime.now());

        // Lưu tin nhắn vào cơ sở dữ liệu
        chatSessionService.saveMessage(messageDTO);
        
        // Xóa cache tin nhắn của session này để đảm bảo dữ liệu mới nhất được tải lại
        chatSessionService.invalidateMessageCache(messageDTO.getSessionId());
        
        // Gửi tin nhắn qua WebSocket đến kênh của phiên chat cụ thể
        ChatSession chatSession = chatSessionService.getChatSessionById(messageDTO.getSessionId());
        String destination = "/topic/chat." + chatSession.getSessionId();
        messagingTemplate.convertAndSend(destination, messageDTO);
        
        // Gửi thông báo về tin nhắn mới cho các client đang lắng nghe (thường là bác sĩ)
        Map<String, Object> newMessageNotification = new HashMap<>();
        newMessageNotification.put("type", "NEW_MESSAGE");
        newMessageNotification.put("sessionId", chatSession.getSessionId());
        newMessageNotification.put("userId", messageDTO.getSenderId()); // ID người dùng gửi
        newMessageNotification.put("messageContent", messageDTO.getMessageContent()); // Nội dung tóm tắt
        newMessageNotification.put("senderId", messageDTO.getSenderId());
        newMessageNotification.put("senderName", chatSession.getUser().getFullName()); // Tên người dùng
        messagingTemplate.convertAndSend("/topic/session-updates", newMessageNotification);
    }

    /**
     * Kiểm tra xem người dùng hiện tại có phiên chat nào đang hoạt động hay không.
     * Được sử dụng bởi client để xác định trạng thái chat khi tải trang.
     *
     * @param session HttpSession chứa thông tin người dùng đăng nhập.
     * @return ResponseEntity chứa trạng thái `hasActiveChat` (boolean) và thông tin `chatSession` nếu có.
     */
    @GetMapping("/check-active-chat")
    @ResponseBody
    public ResponseEntity<?> checkActiveChat(HttpSession session) {
        User loggedInUser = getLoggedInUser(session);
        
        if (loggedInUser == null) {
            // Nếu chưa đăng nhập, mặc định không có chat nào hoạt động
            return ResponseEntity.ok(Map.of("hasActiveChat", false));
        }

        Optional<ChatSession> activeUserChat = chatSessionService.getUserActiveChat(loggedInUser);
        return ResponseEntity.ok(Map.of(
            "hasActiveChat", activeUserChat.isPresent(),
            "chatSession", activeUserChat.orElse(null) // Trả về null nếu không có session nào
        ));
    }

    /**
     * Endpoint REST để người dùng gửi tin nhắn (dự phòng hoặc cho các client không dùng WebSocket trực tiếp).
     * Lưu tin nhắn và gửi qua WebSocket tới các client khác.
     *
     * @param messageDTO DTO chứa thông tin tin nhắn.
     * @param session HttpSession.
     * @return ResponseEntity xác nhận thành công hoặc lỗi.
     */
    @PostMapping("/send-message")
    @ResponseBody
    public ResponseEntity<?> sendMessage(@RequestBody MessageDTO messageDTO, HttpSession session) {
        User loggedInUser = getLoggedInUser(session);
        
        // Xác thực người gửi
        if (loggedInUser == null || !loggedInUser.getUserId().equals(messageDTO.getSenderId())) {
            // Chuyển hướng nếu chưa đăng nhập hoặc sai người gửi
            return ResponseEntity.status(HttpStatus.FOUND).location(URI.create("/home")).build();
        }

        // Luôn đặt timestamp là thời gian hiện tại của server
        messageDTO.setSentAt(LocalDateTime.now());

        // Lưu tin nhắn và xóa cache
        chatSessionService.saveMessage(messageDTO);
        chatSessionService.invalidateMessageCache(messageDTO.getSessionId());
        
        // Gửi tin nhắn qua WebSocket
        ChatSession chatSession = chatSessionService.getChatSessionById(messageDTO.getSessionId());
        String destination = "/topic/chat." + chatSession.getSessionId();
        messagingTemplate.convertAndSend(destination, messageDTO);

        return ResponseEntity.ok(Map.of("success", true));
    }

    /**
     * Kết thúc một phiên chat theo yêu cầu của người dùng.
     * Cập nhật trạng thái phiên chat trong DB và gửi thông báo kết thúc qua WebSocket.
     *
     * @param sessionId ID của phiên chat cần kết thúc.
     * @param session HttpSession.
     * @return ResponseEntity xác nhận thành công hoặc lỗi.
     */
    @PostMapping("/end-chat/{sessionId}")
    @ResponseBody
    public ResponseEntity<?> endChat(@PathVariable Integer sessionId, HttpSession session) {
        User loggedInUser = getLoggedInUser(session);
        
        if (loggedInUser == null) {
            return ResponseEntity.status(HttpStatus.FOUND).location(URI.create("/home")).build();
        }

        try {
            chatSessionService.endChatPreserveNotes(sessionId, "Người dùng kết thúc phiên chat");
            
            // Gửi thông báo kết thúc phiên chat qua topic chung
            Map<String, Object> endSessionNotification = new HashMap<>();
            endSessionNotification.put("type", "END_SESSION");
            endSessionNotification.put("sessionId", sessionId);
            endSessionNotification.put("endedByUser", true); // Cho biết người dùng kết thúc
            endSessionNotification.put("endTime", LocalDateTime.now().toString());
            messagingTemplate.convertAndSend("/topic/session-updates", endSessionNotification);
            
            // Gửi thông báo trực tiếp đến kênh chat cụ thể để client cập nhật UI
            messagingTemplate.convertAndSend("/topic/chat." + sessionId, 
                Map.of("type", "CHAT_ENDED", "sessionId", sessionId, "endTime", LocalDateTime.now().toString()));
            
            return ResponseEntity.ok(Map.of("success", true));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Kết thúc phiên chat và đồng thời "like" (tăng lượt thích) cho bác sĩ trong phiên đó.
     * Cập nhật trạng thái phiên chat, tăng lượt thích cho bác sĩ, và gửi thông báo qua WebSocket.
     *
     * @param sessionId ID của phiên chat cần kết thúc.
     * @param session HttpSession.
     * @return ResponseEntity xác nhận thành công (kèm tên bác sĩ) hoặc lỗi.
     */
    @PostMapping("/end-chat-with-like/{sessionId}")
    @ResponseBody
    public ResponseEntity<?> endChatWithLike(@PathVariable Integer sessionId, HttpSession session) {
        User loggedInUser = getLoggedInUser(session);
        
        if (loggedInUser == null) {
            return ResponseEntity.status(HttpStatus.FOUND).location(URI.create("/home")).build();
        }

        try {
            // Kết thúc chat và tăng lượt thích cho bác sĩ
            ChatSession endedSession = chatSessionService.endChatWithLikePreserveNotes(sessionId, "Người dùng kết thúc phiên chat và đánh giá tích cực");
            
            // Gửi thông báo kết thúc phiên chat qua topic chung
            Map<String, Object> endSessionNotification = new HashMap<>();
            endSessionNotification.put("type", "END_SESSION");
            endSessionNotification.put("sessionId", sessionId);
            endSessionNotification.put("endedByUser", true);
            endSessionNotification.put("endTime", endedSession.getEndTime().toString());
            messagingTemplate.convertAndSend("/topic/session-updates", endSessionNotification);
            
            // Gửi thông báo trực tiếp đến kênh chat cụ thể
            messagingTemplate.convertAndSend("/topic/chat." + sessionId, 
                Map.of("type", "CHAT_ENDED", "sessionId", sessionId, "endTime", endedSession.getEndTime().toString()));
            
            // Trả về thành công kèm thông tin bác sĩ đã được like
            return ResponseEntity.ok(Map.of(
                "success", true, 
                "message", "Cảm ơn bạn đã đánh giá bác sĩ!",
                "doctorName", endedSession.getDoctor().getFullName()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Lấy lịch sử tin nhắn của một phiên chat cụ thể.
     * Chỉ người dùng tham gia phiên chat đó hoặc admin mới có quyền truy cập.
     *
     * @param sessionId ID của phiên chat.
     * @param session HttpSession.
     * @return ResponseEntity chứa danh sách tin nhắn và thông tin cơ bản của session, hoặc lỗi.
     */
    @GetMapping("/chat-history/{sessionId}")
    @ResponseBody
    public ResponseEntity<?> getChatHistory(@PathVariable Integer sessionId, HttpSession session) {
        User loggedInUser = getLoggedInUser(session);
        
        if (loggedInUser == null) {
            return ResponseEntity.status(HttpStatus.FOUND).location(URI.create("/home")).build();
        }

        try {
            ChatSession chatSession = chatSessionService.getChatSessionById(sessionId);
            
            // Kiểm tra quyền truy cập: Phải là user, doctor của session hoặc là admin
            if (!chatSession.getUser().getUserId().equals(loggedInUser.getUserId()) && 
                !(chatSession.getDoctor() != null && chatSession.getDoctor().getUserId().equals(loggedInUser.getUserId())) &&
                !"admin".equals(loggedInUser.getRole().getRoleName())) {
                return ResponseEntity.status(HttpStatus.FOUND).location(URI.create("/home")).build(); // Hoặc trả về lỗi 403 Forbidden
            }
            
            List<MessageDTO> messageHistory = chatSessionService.getChatMessageHistory(sessionId);
            
            // Trả về chỉ thông tin cần thiết về session để tránh lỗi tuần hoàn JSON
            Map<String, Object> sessionInfo = new HashMap<>();
            sessionInfo.put("sessionId", chatSession.getSessionId());
            sessionInfo.put("startTime", chatSession.getStartTime());
            sessionInfo.put("endTime", chatSession.getEndTime());
            
            // Tạo response không chứa cấu trúc đối tượng phức tạp
            Map<String, Object> response = new HashMap<>();
            response.put("messages", messageHistory);
            response.put("session", sessionInfo);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * Phương thức trợ giúp để lấy thông tin người dùng đang đăng nhập.
     * Ưu tiên lấy từ SecurityContextHolder, nếu không có thì lấy từ HttpSession.
     *
     * @param session HttpSession.
     * @return Đối tượng User đang đăng nhập hoặc null nếu chưa đăng nhập.
     */
    // Helper method to get the logged-in user from either Spring Security or Session
    private User getLoggedInUser(HttpSession session) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User loggedInUser = null;
        
        if (auth != null && auth.isAuthenticated() && auth.getPrincipal() instanceof User) {
            // Lấy từ context bảo mật nếu có
            loggedInUser = (User) auth.getPrincipal();
        } else {
            // Lấy từ session như một fallback
            loggedInUser = (User) session.getAttribute("loggedInUser");
        }
        
        return loggedInUser;
    }
    
    /**
     * Endpoint để người dùng "like" một bác sĩ.
     * Tăng số lượt thích của bác sĩ trong DB.
     *
     * @param doctorId ID của bác sĩ được like.
     * @param session HttpSession.
     * @return ResponseEntity xác nhận thành công (kèm số lượt thích mới) hoặc lỗi.
     */
    @PostMapping("/like-doctor/{doctorId}")
    @ResponseBody
    public ResponseEntity<?> likeDoctor(@PathVariable Integer doctorId, HttpSession session) {
        User loggedInUser = getLoggedInUser(session);
        
        if (loggedInUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Bạn cần đăng nhập để thực hiện thao tác này"));
        }
        
        try {
            // Gọi service để tăng lượt thích
            User doctor = userService.increaseLikes(doctorId);
            
            // Trả về thành công với số lượt thích đã cập nhật
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Đã tăng lượt thích cho bác sĩ thành công",
                "likes", doctor.getLikes()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * Hiển thị trang danh sách các bác sĩ nổi bật (sắp xếp theo lượt thích).
     *
     * @param model Model để truyền dữ liệu tới view.
     * @param session HttpSession.
     * @return Tên view "popular-doctors".
     */
    @GetMapping("/popular-doctors")
    public String getPopularDoctorsPage(Model model, HttpSession session,
                                       @RequestParam(defaultValue = "0") int page,
                                       @RequestParam(defaultValue = "9") int size) {
        User loggedInUser = getLoggedInUser(session);
        model.addAttribute("loggedInUser", loggedInUser);
        
        // Lấy danh sách bác sĩ nổi bật từ service có phân trang
        try {
            Page<User> doctorPage = userService.getDoctorsByLikesWithPaging(PageRequest.of(page, size));
            model.addAttribute("doctors", doctorPage.getContent()); // Thêm danh sách bác sĩ vào model
            model.addAttribute("doctorPage", doctorPage); // Thêm thông tin phân trang vào model
        } catch (Exception e) {
             // Ghi log lỗi nếu cần
             System.err.println("Error fetching popular doctors: " + e.getMessage());
             model.addAttribute("doctors", List.of()); // Gửi danh sách rỗng nếu có lỗi
             model.addAttribute("fetchError", "Không thể tải danh sách bác sĩ."); // Thêm thông báo lỗi (tùy chọn)
        }
        
        return "popular-doctors";
    }

    /**
     * API endpoint để lấy danh sách các bác sĩ nổi bật (sắp xếp theo lượt thích).
     * Được sử dụng bởi JavaScript để tải động dữ liệu trên trang popular-doctors.
     *
     * @return ResponseEntity chứa danh sách bác sĩ hoặc lỗi.
     */
    @GetMapping("/api/popular-doctors")
    @ResponseBody
    public ResponseEntity<?> getPopularDoctors() {
        try {
            List<User> popularDoctors = userService.getDoctorsByLikes();
            return ResponseEntity.ok(popularDoctors);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                 .body(Map.of("error", "Không thể tải danh sách bác sĩ nổi bật: " + e.getMessage()));
        }
    }
}
