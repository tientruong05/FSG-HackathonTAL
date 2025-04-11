package com.FSGHackathonTAL.service;

import com.FSGHackathonTAL.entity.ChatSession;
import com.FSGHackathonTAL.entity.Message;
import com.FSGHackathonTAL.entity.User;
import com.FSGHackathonTAL.repository.ChatSessionRepository;
import com.FSGHackathonTAL.repository.MessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.FSGHackathonTAL.dto.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ChatSessionService {

    @Autowired
    private ChatSessionRepository chatSessionRepository;
    
    @Autowired
    private MessageRepository messageRepository;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private SimpMessagingTemplate messagingTemplate;
    
    // Cache for active chats to reduce database queries
    private final Map<Integer, Optional<ChatSession>> activeChatCache = new HashMap<>();

    @Transactional
    @CacheEvict(value = {"userActiveChat", "doctorActiveChat", "chatSession"}, allEntries = true)
    public ChatSession startChat(User user, User doctor) {
        Optional<ChatSession> activeUserChat = getUserActiveChat(user);
        if (activeUserChat.isPresent()) {
            ChatSession existingChat = activeUserChat.get();
            if (existingChat.getDoctor().getUserId().equals(doctor.getUserId())) {
                return existingChat;
            }
            existingChat.setEndTime(LocalDateTime.now());
            existingChat.setDoctorNotes("Phiên chat kết thúc tự động do người dùng bắt đầu phiên chat mới");
            chatSessionRepository.save(existingChat);
            activeChatCache.remove(user.getUserId());
        }
        
        Optional<ChatSession> activeDoctorChat = getDoctorActiveChat(doctor);
        if (activeDoctorChat.isPresent()) {
            throw new IllegalStateException("Bác sĩ đang bận với phiên chat khác, vui lòng thử lại sau.");
        }
        
        ChatSession session = new ChatSession();
        session.setUser(user);
        session.setDoctor(doctor);
        session.setStartTime(LocalDateTime.now());
        ChatSession savedSession = chatSessionRepository.save(session);
        
        activeChatCache.put(user.getUserId(), Optional.of(savedSession));
        activeChatCache.put(doctor.getUserId(), Optional.of(savedSession));
        
        if (messagingTemplate != null) {
            Map<String, Object> newSessionNotification = new HashMap<>();
            newSessionNotification.put("type", "NEW_SESSION");
            newSessionNotification.put("sessionId", savedSession.getSessionId());
            newSessionNotification.put("userId", user.getUserId());
            newSessionNotification.put("doctorId", doctor.getUserId());
            messagingTemplate.convertAndSend("/topic/session-updates", newSessionNotification);
        }
        
        return savedSession;
    }

    @Transactional
    @CacheEvict(value = {"userActiveChat", "doctorActiveChat", "chatSession"}, allEntries = true)
    public ChatSession endChat(Integer sessionId, String doctorNotes) {
        ChatSession session = chatSessionRepository.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy phiên trò chuyện"));
        session.setEndTime(LocalDateTime.now());
        session.setDoctorNotes(doctorNotes);
        
        if (session.getUser() != null) {
            activeChatCache.remove(session.getUser().getUserId());
        }
        if (session.getDoctor() != null) {
            activeChatCache.remove(session.getDoctor().getUserId());
        }
        
        return chatSessionRepository.save(session);
    }

    @Transactional
    @CacheEvict(value = {"userActiveChat", "doctorActiveChat", "chatSession"}, allEntries = true)
    public ChatSession endChatWithLike(Integer sessionId, String endReason) {
        ChatSession session = chatSessionRepository.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy phiên trò chuyện"));
                
        // Đặt thời gian kết thúc nhưng giữ nguyên ghi chú của bác sĩ
        session.setEndTime(LocalDateTime.now());
        
        // Chỉ cập nhật ghi chú nếu doctorNotes rỗng hoặc null
        if (session.getDoctorNotes() == null || session.getDoctorNotes().trim().isEmpty()) {
            session.setDoctorNotes(endReason);
        }
        
        // Clear cache entries
        if (session.getUser() != null) {
            activeChatCache.remove(session.getUser().getUserId());
        }
        if (session.getDoctor() != null) {
            activeChatCache.remove(session.getDoctor().getUserId());
            
            // Increase doctor's likes
            userService.increaseLikes(session.getDoctor().getUserId());
        }
        
        return chatSessionRepository.save(session);
    }

    @Transactional
    @CacheEvict(value = {"userActiveChat", "doctorActiveChat", "chatSession"}, allEntries = true)
    public ChatSession endChatPreserveNotes(Integer sessionId, String endReason) {
        ChatSession session = chatSessionRepository.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy phiên trò chuyện"));
                
        // Chỉ đặt thời gian kết thúc, không thay đổi ghi chú của bác sĩ
        session.setEndTime(LocalDateTime.now());
        
        // Chỉ cập nhật ghi chú nếu doctorNotes rỗng hoặc null, giữ nguyên ghi chú hiện có của bác sĩ
        if (session.getDoctorNotes() == null || session.getDoctorNotes().trim().isEmpty()) {
            session.setDoctorNotes(endReason);
        }
        
        // Clear cache entries
        if (session.getUser() != null) {
            activeChatCache.remove(session.getUser().getUserId());
        }
        if (session.getDoctor() != null) {
            activeChatCache.remove(session.getDoctor().getUserId());
        }
        
        return chatSessionRepository.save(session);
    }

    @Transactional
    @CacheEvict(value = {"userActiveChat", "doctorActiveChat", "chatSession"}, allEntries = true)
    public ChatSession endChatWithLikePreserveNotes(Integer sessionId, String endReason) {
        ChatSession session = chatSessionRepository.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy phiên trò chuyện"));
                
        // Đặt thời gian kết thúc nhưng giữ nguyên ghi chú của bác sĩ
        session.setEndTime(LocalDateTime.now());
        
        // Chỉ cập nhật ghi chú nếu doctorNotes rỗng hoặc null
        if (session.getDoctorNotes() == null || session.getDoctorNotes().trim().isEmpty()) {
            session.setDoctorNotes(endReason);
        }
        
        // Clear cache entries
        if (session.getUser() != null) {
            activeChatCache.remove(session.getUser().getUserId());
        }
        if (session.getDoctor() != null) {
            activeChatCache.remove(session.getDoctor().getUserId());
            
            // Increase doctor's likes
            userService.increaseLikes(session.getDoctor().getUserId());
        }
        
        return chatSessionRepository.save(session);
    }

    @Transactional(readOnly = true)
    public List<ChatSession> getUserChatHistory(User user) {
        return chatSessionRepository.findByUser(user);
    }

    @Transactional(readOnly = true)
    public List<ChatSession> getDoctorChatHistory(User doctor) {
        return chatSessionRepository.findByDoctor(doctor);
    }

    @Transactional(readOnly = true)
    public List<ChatSession> getActiveChats(User doctor) {
        return chatSessionRepository.findByDoctorAndEndTimeIsNull(doctor);
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "userActiveChat", key = "#user.userId")
    public Optional<ChatSession> getUserActiveChat(User user) {
        // Check cache first
        if (activeChatCache.containsKey(user.getUserId())) {
            return activeChatCache.get(user.getUserId());
        }
        
        Optional<ChatSession> activeChat = chatSessionRepository.findFirstByUserAndEndTimeIsNullOrderByStartTimeDesc(user);
        
        // Update cache
        activeChatCache.put(user.getUserId(), activeChat);
        
        return activeChat;
    }

    @Transactional(readOnly = true)
    public Optional<ChatSession> getDoctorActiveChat(User doctor) {
        // Xóa cache để lấy dữ liệu mới nhất từ DB
        activeChatCache.remove(doctor.getUserId());
        
        Optional<ChatSession> activeChat = chatSessionRepository.findFirstByDoctorAndEndTimeIsNullOrderByStartTimeDesc(doctor);
        
        // Cập nhật cache
        activeChatCache.put(doctor.getUserId(), activeChat);
        
        return activeChat;
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "chatSession", key = "#sessionId")
    public ChatSession getChatSessionById(Integer sessionId) {
        if (sessionId == null) {
            throw new IllegalArgumentException("Session ID không được để trống");
        }
        return chatSessionRepository.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy phiên trò chuyện với ID: " + sessionId));
    }

    @Transactional
    @CacheEvict(value = "chatSession", key = "#sessionId")
    public void updateDoctorNotes(Integer sessionId, String doctorNotes) {
        ChatSession chatSession = chatSessionRepository.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Chat session not found"));
        chatSession.setDoctorNotes(doctorNotes);
        chatSessionRepository.save(chatSession);
    }
    
    @Transactional(readOnly = true)
    public boolean canUserStartChat(User user) {
        return !getUserActiveChat(user).isPresent();
    }
    
    @Transactional(readOnly = true)
    public boolean canDoctorAcceptChat(User doctor) {
        return !getDoctorActiveChat(doctor).isPresent();
    }

    /**
     * Lưu tin nhắn vào cơ sở dữ liệu
     */
    @Transactional
    public Message saveMessage(MessageDTO messageDTO) {
        // Lấy thông tin phiên chat và người dùng
        ChatSession chatSession = getChatSessionById(messageDTO.getSessionId());
        User sender = userService.getUserById(messageDTO.getSenderId())
            .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người gửi"));
        
        // Tạo đối tượng Message
        Message message = new Message();
        message.setChatSession(chatSession);
        message.setSender(sender);
        message.setMessageContent(messageDTO.getMessageContent());
        message.setSentAt(messageDTO.getSentAt() != null ? messageDTO.getSentAt() : LocalDateTime.now());
        message.setClientId(messageDTO.get_clientId());
        
        // Lưu tin nhắn vào cơ sở dữ liệu
        return messageRepository.save(message);
    }
    
    /**
     * Lưu nhiều tin nhắn cùng lúc để cải thiện hiệu suất
     */
    @Transactional
    public List<Message> saveMessages(List<MessageDTO> messageDTOs) {
        if (messageDTOs == null || messageDTOs.isEmpty()) {
            return new ArrayList<>();
        }
        
        List<Message> messages = messageDTOs.stream().map(dto -> {
            ChatSession chatSession = getChatSessionById(dto.getSessionId());
            User sender = userService.getUserById(dto.getSenderId())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người gửi"));
            
            Message message = new Message();
            message.setChatSession(chatSession);
            message.setSender(sender);
            message.setMessageContent(dto.getMessageContent());
            message.setSentAt(dto.getSentAt() != null ? dto.getSentAt() : LocalDateTime.now());
            message.setClientId(dto.get_clientId());
            
            return message;
        }).collect(Collectors.toList());
        
        return messageRepository.saveAll(messages);
    }
    
    /**
     * Lấy lịch sử tin nhắn của một phiên chat
     */
    @Transactional(readOnly = true)
    @Cacheable(value = "chatMessages", key = "#sessionId")
    public List<MessageDTO> getChatMessageHistory(Integer sessionId) {
        List<Message> messages = messageRepository.findByChatSessionIdOrderBySentAt(sessionId);
        
        // Convert to DTOs using stream for better performance
        return messages.stream().map(message -> {
            MessageDTO dto = new MessageDTO(
                message.getChatSession().getSessionId(),
                message.getSender().getUserId(),
                message.getMessageContent(),
                message.getSentAt()
            );
            dto.set_clientId(message.getClientId());
            return dto;
        }).collect(Collectors.toList());
    }
    
    /**
     * Xóa cache messages khi có tin nhắn mới
     */
    @CacheEvict(value = "chatMessages", key = "#sessionId")
    public void invalidateMessageCache(Integer sessionId) {
        // Method để xóa cache khi cần
    }

    public ChatSession createChatSession(User user, User doctor) {
        if (user == null || doctor == null) {
            throw new IllegalArgumentException("User and doctor cannot be null");
        }

        // Kiểm tra xem người dùng đã có phiên chat đang hoạt động chưa
        Optional<ChatSession> activeChat = getUserActiveChat(user);
        if (activeChat.isPresent()) {
            throw new IllegalStateException("User already has an active chat session");
        }

        ChatSession chatSession = new ChatSession();
        chatSession.setUser(user);
        chatSession.setDoctor(doctor);
        chatSession.setStartTime(LocalDateTime.now());

        ChatSession savedSession = chatSessionRepository.save(chatSession);
        
        // Gửi thông báo qua WebSocket về phiên chat mới
        if (messagingTemplate != null) {
            Map<String, Object> newSessionNotification = new HashMap<>();
            newSessionNotification.put("type", "NEW_SESSION");
            newSessionNotification.put("sessionId", savedSession.getSessionId());
            newSessionNotification.put("userId", user.getUserId());
            newSessionNotification.put("doctorId", doctor.getUserId());
            messagingTemplate.convertAndSend("/topic/session-updates", newSessionNotification);
        }
        
        return savedSession;
    }

    /**
     * Lấy danh sách phiên chat của bác sĩ với phân trang
     */
    @Transactional(readOnly = true)
    public Page<ChatSession> getDoctorChatHistoryPaged(User doctor, Pageable pageable) {
        return chatSessionRepository.findByDoctor(doctor, pageable);
    }
}
