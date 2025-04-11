package com.FSGHackathonTAL.controller;

import com.FSGHackathonTAL.dto.MessageDTO;
import com.FSGHackathonTAL.entity.ChatSession;
import com.FSGHackathonTAL.entity.User;
import com.FSGHackathonTAL.entity.Message;
import com.FSGHackathonTAL.service.ChatSessionService;
import com.FSGHackathonTAL.service.UserService;
import com.FSGHackathonTAL.service.DoctorAvailabilityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.ui.Model;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.time.LocalDateTime;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import java.net.URI;
import java.util.stream.Collectors;

/**
 * Controller quản lý các chức năng liên quan đến giao diện chat của bác sĩ.
 * Bao gồm xử lý tin nhắn WebSocket, hiển thị danh sách phiên chat, chi tiết phiên chat,
 * gửi tin nhắn, kết thúc phiên chat, lưu ghi chú, và quản lý trạng thái online của bác sĩ.
 * Yêu cầu quyền 'doctor' để truy cập hầu hết các endpoint.
 */
@Controller
public class DoctorChatController {

    @Autowired
    private ChatSessionService chatSessionService;

    @Autowired
    private UserService userService;
    
    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private DoctorAvailabilityService doctorAvailabilityService;

    /**
     * Xử lý tin nhắn đến từ bác sĩ qua WebSocket.
     * Lưu tin nhắn, cập nhật thời gian, xóa cache và gửi lại tới kênh chat của phiên.
     *
     * @param messageDTO DTO chứa thông tin tin nhắn gửi từ bác sĩ.
     */
    @MessageMapping("/doctor.sendMessage")
    public void sendMessage(MessageDTO messageDTO) {
        try {
            User sender = userService.getUserById(messageDTO.getSenderId())
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng gửi tin nhắn (bác sĩ)"));
            
            // Luôn đặt timestamp theo giờ server
            messageDTO.setSentAt(LocalDateTime.now());
            
            // Lưu tin nhắn vào cơ sở dữ liệu
            Message savedMessage = chatSessionService.saveMessage(messageDTO);
            
            // Xóa cache tin nhắn để đảm bảo dữ liệu mới nhất
            chatSessionService.invalidateMessageCache(messageDTO.getSessionId());
            
            // Chuyển tiếp tin nhắn tới kênh WebSocket của phiên chat cụ thể
            String destination = "/topic/chat." + messageDTO.getSessionId();
            messagingTemplate.convertAndSend(destination, messageDTO);
            
            // Log để debug
            System.out.println("Đã lưu tin nhắn từ Bác sĩ vào DB: " + savedMessage.getMessageId() + 
                ", Session: " + savedMessage.getChatSession().getSessionId() + 
                ", Doctor: " + savedMessage.getSender().getUserId() + 
                ", Content: " + savedMessage.getMessageContent() + 
                ", Time: " + savedMessage.getSentAt());
                
            // Thông báo tin nhắn mới tới người dùng đã được xử lý khi gửi tới kênh chat riêng.
        } catch (Exception e) {
            System.err.println("Lỗi khi bác sĩ gửi tin nhắn qua WebSocket: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Hiển thị trang quản lý chat của bác sĩ.
     * Lấy danh sách phiên chat (phân trang, sắp xếp theo thời gian bắt đầu giảm dần),
     * kiểm tra phiên chat đang hoạt động (nếu có), và truyền dữ liệu tới view.
     *
     * @param page Số trang hiện tại.
     * @param size Kích thước trang.
     * @param model Model để truyền dữ liệu.
     * @param session HttpSession để lấy thông tin bác sĩ đăng nhập.
     * @return Tên view "doctor-chats" hoặc redirect nếu không phải bác sĩ.
     */
    @GetMapping("/doctor/chats")
    public String getDoctorChats(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model, 
            HttpSession session) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null || !"doctor".equals(loggedInUser.getRole().getRoleName())) {
            return "redirect:/home"; // Chỉ bác sĩ mới được truy cập
        }
        
        try {
            // Xác thực tham số phân trang
            page = Math.max(0, page);
            size = Math.max(1, Math.min(size, 50)); // Giới hạn size
            
            // Tạo đối tượng Pageable với sắp xếp theo startTime giảm dần (mới nhất trước)
            Pageable pageable = PageRequest.of(page, size, org.springframework.data.domain.Sort.by("startTime").descending());
            
            // Lấy lịch sử chat của bác sĩ theo phân trang
            Page<ChatSession> chatSessionPage = chatSessionService.getDoctorChatHistoryPaged(loggedInUser, pageable);
            model.addAttribute("chatSessions", chatSessionPage.getContent());
            model.addAttribute("chatSessionPage", chatSessionPage);
            model.addAttribute("loggedInUser", loggedInUser);
            
            // Kiểm tra xem bác sĩ có phiên chat nào đang hoạt động không
            Optional<ChatSession> activeChat = chatSessionService.getDoctorActiveChat(loggedInUser);
            if (activeChat.isPresent()) {
                model.addAttribute("hasActiveChat", true);
                model.addAttribute("activeSession", activeChat.get());
            } else {
                model.addAttribute("hasActiveChat", false);
            }
            
            return "doctor-chats";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Đã xảy ra lỗi khi tải danh sách phiên chat: " + e.getMessage());
            // Cung cấp giá trị mặc định để tránh lỗi view
            model.addAttribute("chatSessions", List.of());
            model.addAttribute("chatSessionPage", Page.empty());
            model.addAttribute("loggedInUser", loggedInUser);
            model.addAttribute("hasActiveChat", false);
            return "doctor-chats";
        }
    }

    /**
     * Xử lý việc bác sĩ gửi tin nhắn thông qua form POST.
     * Lưu tin nhắn và gửi qua WebSocket để cập nhật real-time.
     * (Thường dùng làm fallback hoặc cho các kịch bản không dùng WebSocket trực tiếp).
     *
     * @param sessionId ID của phiên chat.
     * @param messageContent Nội dung tin nhắn.
     * @param _clientId ID tạm thời của client (nếu có, dùng để tránh gửi lại cho chính client đó).
     * @param page Trang hiện tại (để redirect).
     * @param size Kích thước trang (để redirect).
     * @param session HttpSession.
     * @return Redirect về trang chi tiết phiên chat.
     */
    @PostMapping("/doctor/chats/send")
    public String sendDoctorChatMessage(
            @RequestParam Integer sessionId,
            @RequestParam String messageContent,
            @RequestParam(required = false) String _clientId, // Client ID tạm thời để tránh echo
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpSession session) {
        
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null || !"doctor".equals(loggedInUser.getRole().getRoleName())) {
            return "redirect:/home";
        }
        
        try {
            // Kiểm tra xem phiên chat có thuộc về bác sĩ này không
            ChatSession chatSession = chatSessionService.getChatSessionById(sessionId);
            if (!chatSession.getDoctor().getUserId().equals(loggedInUser.getUserId())) {
                // Không thuộc quyền -> redirect về danh sách chat
                return "redirect:/doctor/chats"; 
            }
            
            // Kiểm tra xem phiên chat có đang hoạt động không (chưa kết thúc)
            if (chatSession.getEndTime() != null) {
                 // Phiên chat đã kết thúc, không cho gửi thêm
                 // Redirect về trang chi tiết với thông báo (có thể thêm flash attribute)
                return "redirect:/doctor/chats/" + sessionId + "?page=" + page + "&size=" + size;
            }
            
            // Tạo message DTO
            LocalDateTime sentAt = LocalDateTime.now();
            MessageDTO messageDTO = new MessageDTO(sessionId, loggedInUser.getUserId(), messageContent, sentAt);
            
            // Thêm client ID nếu có để WebSocket có thể bỏ qua việc gửi lại cho người gửi
            if (_clientId != null && !_clientId.isEmpty()) {
                messageDTO.set_clientId(_clientId);
            }
            
            // Lưu tin nhắn vào cơ sở dữ liệu
            Message savedMessage = chatSessionService.saveMessage(messageDTO);
            
            // Log để debug
            System.out.println("Bác sĩ đã gửi tin nhắn (POST): " + savedMessage.getMessageId() + 
                ", Session: " + savedMessage.getChatSession().getSessionId() + 
                ", Doctor: " + savedMessage.getSender().getUserId() + 
                ", Content: " + savedMessage.getMessageContent() + 
                ", Time: " + savedMessage.getSentAt());
            
            // Gửi qua WebSocket để cập nhật realtime cho client khác (người dùng)
            // Sử dụng hàm sendMessage đã định nghĩa ở trên hoặc trực tiếp template
            this.sendMessage(messageDTO); // Gọi lại hàm xử lý WebSocket
            
            // Redirect về trang chi tiết của phiên chat đó
            return "redirect:/doctor/chats/" + sessionId + "?page=" + page + "&size=" + size;
        } catch (Exception e) {
            System.err.println("Lỗi khi bác sĩ gửi tin nhắn (POST): " + e.getMessage());
            e.printStackTrace();
            // Redirect về trang danh sách chat với thông báo lỗi (nên dùng RedirectAttributes)
            return "redirect:/doctor/chats?page=" + page + "&size=" + size;
        }
    }

    /**
     * Lưu ghi chú của bác sĩ cho một phiên chat cụ thể.
     *
     * @param sessionId ID của phiên chat.
     * @param doctorNotes Nội dung ghi chú.
     * @param page Trang hiện tại (để redirect).
     * @param size Kích thước trang (để redirect).
     * @param session HttpSession.
     * @return Redirect về trang chi tiết phiên chat.
     */
    @PostMapping("/doctor/chats/{sessionId}/save-notes")
    public String saveDoctorNotes(
            @PathVariable Integer sessionId,
            @RequestParam String doctorNotes,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpSession session) {
        
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null || !"doctor".equals(loggedInUser.getRole().getRoleName())) {
            return "redirect:/home";
        }
        
        try {
        	// Kiểm tra xem phiên chat có thuộc về bác sĩ này không
            ChatSession chatSession = chatSessionService.getChatSessionById(sessionId);
            if (!chatSession.getDoctor().getUserId().equals(loggedInUser.getUserId())) {
                return "redirect:/doctor/chats";
            }
            
            chatSessionService.updateDoctorNotes(sessionId, doctorNotes);
            // Có thể thêm FlashAttribute thông báo thành công
        } catch (Exception e) {
        	// Có thể thêm FlashAttribute thông báo lỗi
        	System.err.println("Lỗi khi lưu ghi chú của bác sĩ: " + e.getMessage());
        }
        
        // Redirect về trang chi tiết của phiên chat
        return "redirect:/doctor/chats/" + sessionId + "?page=" + page + "&size=" + size;
    }

    /**
     * Hiển thị chi tiết một phiên chat cụ thể trong giao diện quản lý của bác sĩ.
     * Tải thông tin phiên chat được chọn và danh sách các phiên chat khác (phân trang).
     *
     * @param sessionId ID của phiên chat cần hiển thị chi tiết.
     * @param page Số trang hiện tại của danh sách chat.
     * @param size Kích thước trang của danh sách chat.
     * @param model Model để truyền dữ liệu.
     * @param session HttpSession.
     * @return Tên view "doctor-chats".
     */
    @GetMapping("/doctor/chats/{sessionId}")
    public String getDoctorChat(
            @PathVariable Integer sessionId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model,
            HttpSession session) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null || !"doctor".equals(loggedInUser.getRole().getRoleName())) {
            return "redirect:/home";
        }
        
        try {
            // Xác thực tham số phân trang
            page = Math.max(0, page);
            size = Math.max(1, Math.min(size, 50));
            
            // Tạo đối tượng Pageable
            Pageable pageable = PageRequest.of(page, size, org.springframework.data.domain.Sort.by("startTime").descending());
            
            // Lấy thông tin phiên chat được chọn
            ChatSession selectedSession = chatSessionService.getChatSessionById(sessionId);
            // Kiểm tra xem phiên chat có thuộc về bác sĩ này không
            if (!selectedSession.getDoctor().getUserId().equals(loggedInUser.getUserId())) {
                return "redirect:/doctor/chats"; // Không thuộc quyền -> về trang danh sách
            }
            
            model.addAttribute("selectedSession", selectedSession);
            // Lấy lịch sử tin nhắn cho session được chọn (có thể thực hiện ở đây hoặc bằng AJAX)
            // List<MessageDTO> messages = chatSessionService.getChatMessageHistory(sessionId);
            // model.addAttribute("messages", messages);
            
            // Lấy danh sách các phiên chat khác để hiển thị bên cạnh
            Page<ChatSession> chatSessionPage = chatSessionService.getDoctorChatHistoryPaged(loggedInUser, pageable);
            model.addAttribute("chatSessions", chatSessionPage.getContent());
            model.addAttribute("chatSessionPage", chatSessionPage);
            model.addAttribute("loggedInUser", loggedInUser);
            
            // Kiểm tra xem bác sĩ có đang tham gia phiên chat nào đang hoạt động không
            Optional<ChatSession> activeChat = chatSessionService.getDoctorActiveChat(loggedInUser);
            model.addAttribute("hasActiveChat", activeChat.isPresent());
            activeChat.ifPresent(chat -> model.addAttribute("activeSession", chat));

            return "doctor-chats"; // Trả về cùng view, nhưng có thêm selectedSession
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Đã xảy ra lỗi khi tải chi tiết phiên chat: " + e.getMessage());
            // Cung cấp giá trị mặc định
            model.addAttribute("chatSessions", List.of());
            model.addAttribute("chatSessionPage", Page.empty());
            model.addAttribute("loggedInUser", loggedInUser);
            model.addAttribute("hasActiveChat", false);
            return "doctor-chats"; // Quay về view chính với thông báo lỗi
        }
    }

    /**
     * Endpoint REST để bác sĩ kết thúc một phiên chat.
     * Cập nhật ghi chú của bác sĩ, kết thúc phiên chat trong DB, và gửi thông báo WebSocket.
     *
     * @param sessionId ID của phiên chat cần kết thúc.
     * @param doctorNotes Ghi chú cuối cùng của bác sĩ.
     * @param httpSession HttpSession.
     * @return ResponseEntity xác nhận thành công hoặc lỗi.
     */
    @PostMapping("/doctor/chats/end/{sessionId}")
    public ResponseEntity<?> endChatSession(
            @PathVariable Integer sessionId,
            @RequestParam String doctorNotes, // Nhận ghi chú từ request body hoặc form
            HttpSession httpSession) {
        User doctor = (User) httpSession.getAttribute("loggedInUser");
        if (doctor == null || !"doctor".equals(doctor.getRole().getRoleName())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Không có quyền truy cập"));
        }

        try {
            ChatSession chatSession = chatSessionService.getChatSessionById(sessionId);
            // Xác thực bác sĩ sở hữu phiên chat
            if (!chatSession.getDoctor().getUserId().equals(doctor.getUserId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Bạn không có quyền kết thúc phiên chat này"));
            }
            
            // Kiểm tra xem phiên chat đã kết thúc chưa
            if (chatSession.getEndTime() != null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Phiên chat đã được kết thúc trước đó"));
            }

            // Cập nhật ghi chú trước khi kết thúc
            chatSessionService.updateDoctorNotes(sessionId, doctorNotes);
            
            // Kết thúc phiên chat
            ChatSession endedSession = chatSessionService.endChatPreserveNotes(sessionId, "Bác sĩ kết thúc phiên chat");

            // Gửi thông báo kết thúc phiên chat qua WebSocket
            Map<String, Object> endSessionNotification = new HashMap<>();
            endSessionNotification.put("type", "END_SESSION");
            endSessionNotification.put("sessionId", sessionId);
            endSessionNotification.put("endedByDoctor", true); // Cho biết bác sĩ kết thúc
            endSessionNotification.put("endTime", endedSession.getEndTime().toString());
            messagingTemplate.convertAndSend("/topic/session-updates", endSessionNotification);

            // Gửi thông báo trực tiếp đến kênh chat cụ thể
            messagingTemplate.convertAndSend("/topic/chat." + sessionId, 
                Map.of("type", "CHAT_ENDED", "sessionId", sessionId, "endTime", endedSession.getEndTime().toString()));

            return ResponseEntity.ok(Map.of("success", true, "message", "Phiên chat đã kết thúc thành công"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                 .body(Map.of("error", "Lỗi khi kết thúc phiên chat: " + e.getMessage()));
        }
    }

    /**
     * API endpoint để lấy chi tiết (bao gồm lịch sử tin nhắn) của một phiên chat.
     * Được sử dụng bởi JavaScript để tải động nội dung chat.
     *
     * @param sessionId ID của phiên chat.
     * @param session HttpSession.
     * @return ResponseEntity chứa thông tin chi tiết session và danh sách tin nhắn, hoặc lỗi.
     */
    @GetMapping("/doctor/chats/{sessionId}/details")
    public @ResponseBody ResponseEntity<?> getChatSessionDetails(@PathVariable Integer sessionId, HttpSession session) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null || !"doctor".equals(loggedInUser.getRole().getRoleName())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Không có quyền truy cập"));
        }

        try {
            ChatSession chatSession = chatSessionService.getChatSessionById(sessionId);
            // Xác thực bác sĩ sở hữu phiên chat
            if (!chatSession.getDoctor().getUserId().equals(loggedInUser.getUserId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Bạn không có quyền xem chi tiết phiên chat này"));
            }

            List<MessageDTO> messages = chatSessionService.getChatMessageHistory(sessionId);
            
            // Trả về thông tin cần thiết để tránh lỗi tuần hoàn JSON và lộ thông tin không cần thiết
            Map<String, Object> sessionDetails = new HashMap<>();
            sessionDetails.put("sessionId", chatSession.getSessionId());
            sessionDetails.put("startTime", chatSession.getStartTime());
            sessionDetails.put("endTime", chatSession.getEndTime());
            sessionDetails.put("doctorNotes", chatSession.getDoctorNotes());
            sessionDetails.put("userName", chatSession.getUser().getFullName()); // Gửi tên người dùng
            sessionDetails.put("userId", chatSession.getUser().getUserId());
            sessionDetails.put("userImage", chatSession.getUser().getImage());
            sessionDetails.put("doctorName", chatSession.getDoctor().getFullName()); // Gửi tên bác sĩ
            sessionDetails.put("doctorId", chatSession.getDoctor().getUserId());
            sessionDetails.put("doctorImage", chatSession.getDoctor().getImage());
            
            Map<String, Object> response = new HashMap<>();
            response.put("session", sessionDetails);
            response.put("messages", messages);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                 .body(Map.of("error", "Lỗi khi lấy chi tiết phiên chat: " + e.getMessage()));
        }
    }
    
    /**
     * API endpoint để lấy trạng thái hiện tại của bác sĩ (online/offline và thông tin phiên chat đang hoạt động).
     * Được sử dụng bởi JavaScript để cập nhật UI.
     *
     * @param session HttpSession.
     * @return ResponseEntity chứa trạng thái online và thông tin phiên chat hoạt động (nếu có).
     */
    @GetMapping("/api/doctor/status")
    public @ResponseBody ResponseEntity<Map<String, Object>> getDoctorStatus(HttpSession session) {
        User doctor = (User) session.getAttribute("loggedInUser");
        Map<String, Object> response = new HashMap<>();

        if (doctor == null || !"doctor".equals(doctor.getRole().getRoleName())) {
            response.put("isOnline", false);
            response.put("hasActiveChat", false);
            return ResponseEntity.ok(response); // Trả về trạng thái mặc định nếu không phải bác sĩ
        }
        
        try {
        	// Lấy lại thông tin user mới nhất từ DB để đảm bảo trạng thái isOnline là chính xác
        	User currentDoctorState = userService.getUserById(doctor.getUserId())
        	        .orElseThrow(() -> new RuntimeException("Không tìm thấy thông tin bác sĩ"));
        	        
            response.put("isOnline", currentDoctorState.getIsOnline());

            Optional<ChatSession> activeChat = chatSessionService.getDoctorActiveChat(currentDoctorState);
            response.put("hasActiveChat", activeChat.isPresent());
            if (activeChat.isPresent()) {
                ChatSession sessionInfo = activeChat.get();
                Map<String, Object> activeSessionDetails = new HashMap<>();
                activeSessionDetails.put("sessionId", sessionInfo.getSessionId());
                activeSessionDetails.put("userName", sessionInfo.getUser().getFullName());
                activeSessionDetails.put("startTime", sessionInfo.getStartTime().toString());
                response.put("activeSession", activeSessionDetails);
            }

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            // Log lỗi
            System.err.println("Lỗi khi lấy trạng thái bác sĩ: " + e.getMessage());
            // Trả về trạng thái lỗi hoặc mặc định
            response.put("isOnline", false); 
            response.put("hasActiveChat", false);
            response.put("error", "Không thể lấy trạng thái bác sĩ.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Endpoint REST để bác sĩ gửi tin nhắn.
     * Lưu tin nhắn và gửi qua WebSocket.
     *
     * @param messageDTO DTO chứa thông tin tin nhắn.
     * @param session HttpSession.
     * @return ResponseEntity xác nhận thành công hoặc lỗi.
     */
    @PostMapping("/doctor/send-message")
    @ResponseBody
    public ResponseEntity<?> sendMessageRest(@RequestBody MessageDTO messageDTO, HttpSession session) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        
        // Xác thực người gửi phải là bác sĩ
        if (loggedInUser == null || !"doctor".equals(loggedInUser.getRole().getRoleName()) || !loggedInUser.getUserId().equals(messageDTO.getSenderId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Không có quyền gửi tin nhắn này"));
        }

        try {
            // Kiểm tra phiên chat tồn tại và thuộc về bác sĩ
            ChatSession chatSession = chatSessionService.getChatSessionById(messageDTO.getSessionId());
            if (!chatSession.getDoctor().getUserId().equals(loggedInUser.getUserId())) {
                 return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Phiên chat không thuộc về bạn"));
            }
            
            // Kiểm tra phiên chat có đang hoạt động không
            if (chatSession.getEndTime() != null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Không thể gửi tin nhắn vào phiên chat đã kết thúc"));
            }

            // Luôn đặt timestamp theo giờ server
            messageDTO.setSentAt(LocalDateTime.now());

            // Lưu tin nhắn và xóa cache
            chatSessionService.saveMessage(messageDTO);
            chatSessionService.invalidateMessageCache(messageDTO.getSessionId());
            
            // Gửi tin nhắn qua WebSocket tới kênh của phiên chat
            String destination = "/topic/chat." + chatSession.getSessionId();
            messagingTemplate.convertAndSend(destination, messageDTO);

            return ResponseEntity.ok(Map.of("success", true));
        } catch (Exception e) {
             System.err.println("Lỗi khi bác sĩ gửi tin nhắn (REST): " + e.getMessage());
             return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                  .body(Map.of("error", "Lỗi máy chủ khi gửi tin nhắn: " + e.getMessage()));
        }
    }

    /**
     * API endpoint để lấy danh sách các phiên chat của bác sĩ (chủ yếu là ID và tên người dùng).
     * Được sử dụng bởi JavaScript để cập nhật danh sách chat.
     *
     * @param session HttpSession.
     * @return ResponseEntity chứa danh sách phiên chat (thông tin cơ bản) hoặc lỗi.
     */
    @GetMapping("/doctor/chats/list")
    @ResponseBody
    public ResponseEntity<?> getDoctorChatList(HttpSession session) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null || !"doctor".equals(loggedInUser.getRole().getRoleName())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Không có quyền truy cập"));
        }
        try {
            List<ChatSession> sessions = chatSessionService.getDoctorChatHistory(loggedInUser);
            // Chuyển đổi sang DTO hoặc Map chỉ chứa thông tin cần thiết
            List<Map<String, Object>> sessionList = sessions.stream().map(s -> {
                Map<String, Object> map = new HashMap<>();
                map.put("sessionId", s.getSessionId());
                map.put("userName", s.getUser().getFullName());
                map.put("startTime", s.getStartTime());
                map.put("endTime", s.getEndTime());
                return map;
            }).collect(Collectors.toList());
            return ResponseEntity.ok(sessionList);
        } catch (Exception e) {
             return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                  .body(Map.of("error", "Lỗi khi lấy danh sách chat: " + e.getMessage()));
        }
    }

    /**
     * API endpoint để bác sĩ bật/tắt trạng thái trực tuyến (online).
     * Cập nhật trạng thái trong DB và gửi thông báo qua WebSocket.
     *
     * @param session HttpSession.
     * @return ResponseEntity xác nhận trạng thái mới hoặc lỗi.
     */
    @PostMapping("/doctor/toggle-online")
    @ResponseBody
    public ResponseEntity<?> toggleOnlineStatus(HttpSession session) {
        User doctor = (User) session.getAttribute("loggedInUser");
        if (doctor == null || !"doctor".equals(doctor.getRole().getRoleName())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Không có quyền truy cập"));
        }

        try {
            // Lấy trạng thái mới nhất từ DB trước khi thay đổi
            User currentDoctor = userService.getUserById(doctor.getUserId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy thông tin bác sĩ"));
                    
            boolean newStatus = !currentDoctor.getIsOnline(); // Đảo ngược trạng thái hiện tại
            currentDoctor.setIsOnline(newStatus);
            userService.saveUser(currentDoctor);
            
            // Gửi thông báo cập nhật trạng thái qua WebSocket đến các client quan tâm
            Map<String, Object> statusUpdate = new HashMap<>();
            statusUpdate.put("type", "DOCTOR_STATUS_UPDATE");
            statusUpdate.put("doctorId", currentDoctor.getUserId());
            statusUpdate.put("isOnline", newStatus);
            messagingTemplate.convertAndSend("/topic/doctor-status", statusUpdate);
            
            return ResponseEntity.ok(Map.of("success", true, "isOnline", newStatus));
        } catch (Exception e) {
            System.err.println("Lỗi khi thay đổi trạng thái online: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                 .body(Map.of("error", "Lỗi khi thay đổi trạng thái: " + e.getMessage()));
        }
    }
    
    /**
     * Hiển thị trang lựa chọn bác sĩ cho người dùng.
     * Tải danh sách các bác sĩ đang trực tuyến và truyền tới view.
     * (Lưu ý: Endpoint này có vẻ phù hợp hơn ở một controller khác như UserController hoặc PageController).
     *
     * @param model Model để truyền dữ liệu.
     * @param session HttpSession.
     * @param request HttpServletRequest (không rõ mục đích sử dụng ở đây).
     * @return Tên view "doctor-selection".
     */
     @GetMapping("/doctor-selection")
    public String showDoctorSelection(Model model, HttpSession session, HttpServletRequest request) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null) {
            return "redirect:/home"; // Cần đăng nhập để chọn bác sĩ
        }

        try {
        	// Kiểm tra xem người dùng đã có phiên chat nào đang hoạt động chưa
            Optional<ChatSession> activeChat = chatSessionService.getUserActiveChat(loggedInUser);
            if (activeChat.isPresent()) {
                // Nếu có, chuyển hướng đến trang chat hiện tại
                return "redirect:/chat/" + activeChat.get().getSessionId();
            }
        
            List<User> onlineDoctors = userService.getOnlineDoctors();
            model.addAttribute("onlineDoctors", onlineDoctors);
            model.addAttribute("loggedInUser", loggedInUser);
            return "doctor-selection";
        } catch (Exception e) {
        	model.addAttribute("errorMessage", "Lỗi khi tải danh sách bác sĩ: " + e.getMessage());
        	model.addAttribute("onlineDoctors", List.of());
        	model.addAttribute("loggedInUser", loggedInUser);
        	return "doctor-selection";
        }
    }

    /**
     * Xử lý việc người dùng chọn một bác sĩ để bắt đầu phiên chat mới.
     * Tạo phiên chat mới trong DB và chuyển hướng người dùng đến trang chat.
     *
     * @param doctorId ID của bác sĩ được chọn.
     * @param session HttpSession.
     * @return Redirect tới trang chat của phiên mới hoặc trang chọn bác sĩ nếu có lỗi.
     */
    @PostMapping("/select-doctor")
    public String selectDoctor(@RequestParam Integer doctorId, HttpSession session) {
        User user = (User) session.getAttribute("loggedInUser");
        if (user == null) {
            return "redirect:/home";
        }

        try {
            // Kiểm tra lại xem người dùng có đang chat không phòng trường hợp race condition
            Optional<ChatSession> existingActiveChat = chatSessionService.getUserActiveChat(user);
            if (existingActiveChat.isPresent()) {
                return "redirect:/chat/" + existingActiveChat.get().getSessionId();
            }
            
            // Lấy thông tin bác sĩ
            User doctor = userService.getUserById(doctorId)
                 .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy bác sĩ với ID: " + doctorId));

            // Tạo phiên chat mới
            ChatSession newSession = chatSessionService.createChatSession(user, doctor);
            
            // Gửi thông báo phiên chat mới qua WebSocket cho bác sĩ và các admin (nếu cần)
            Map<String, Object> newSessionNotification = new HashMap<>();
            newSessionNotification.put("type", "NEW_SESSION");
            newSessionNotification.put("sessionId", newSession.getSessionId());
            newSessionNotification.put("userId", newSession.getUser().getUserId());
            newSessionNotification.put("userName", newSession.getUser().getFullName());
            newSessionNotification.put("doctorId", newSession.getDoctor().getUserId());
            messagingTemplate.convertAndSend("/topic/session-updates", newSessionNotification);
            
            // Chuyển hướng người dùng đến trang chat mới
            return "redirect:/chat/" + newSession.getSessionId();
        } catch (IllegalArgumentException e) {
            // Xử lý lỗi cụ thể (ví dụ: bác sĩ không online, người dùng đang chat...)
            // Nên thêm RedirectAttributes để hiển thị lỗi
            System.err.println("Lỗi khi chọn bác sĩ: " + e.getMessage());
            return "redirect:/doctor-selection"; 
        } catch (Exception e) {
            // Xử lý lỗi chung
            System.err.println("Lỗi không xác định khi bắt đầu chat: " + e.getMessage());
            return "redirect:/doctor-selection";
        }
    }
    
    /**
     * API Endpoint để lấy nội dung động cho trang chọn bác sĩ (ví dụ: danh sách bác sĩ online).
     * Được sử dụng bởi JavaScript để cập nhật trang mà không cần tải lại toàn bộ.
     *
     * @param model Model.
     * @param session HttpSession.
     * @return Tên fragment Thymeleaf "doctor-selection-content".
     */
    public String getDoctorSelectionContent(Model model, HttpSession session) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        model.addAttribute("loggedInUser", loggedInUser);
        try {
            List<User> onlineDoctors = userService.getOnlineDoctors();
            model.addAttribute("onlineDoctors", onlineDoctors);
        } catch (Exception e) {
            model.addAttribute("onlineDoctors", List.of());
            model.addAttribute("ajaxErrorMessage", "Lỗi khi tải lại danh sách bác sĩ.");
        }
        return "fragments/doctor-selection-content :: doctorListFragment"; // Trả về fragment
    }
}
