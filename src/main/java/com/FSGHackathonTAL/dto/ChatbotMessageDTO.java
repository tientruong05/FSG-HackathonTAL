package com.FSGHackathonTAL.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Data Transfer Object (DTO) đại diện cho một tin nhắn trong cuộc trò chuyện với chatbot.
 * Được sử dụng để truyền dữ liệu tin nhắn giữa client và server.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatbotMessageDTO {
    /**
     * ID của người gửi tin nhắn (thường là ID của người dùng).
     */
    private Integer senderId;

    /**
     * Nội dung của tin nhắn.
     */
    private String messageContent;

    /**
     * Thời điểm tin nhắn được gửi.
     */
    private LocalDateTime sentAt;

    /**
     * Cờ đánh dấu tin nhắn này có phải từ người dùng (true) hay từ chatbot (false).
     */
    private boolean fromUser;

    /**
     * ID định danh phía client, dùng để theo dõi và cập nhật trạng thái tin nhắn trên giao diện.
     */
    private String clientId; // For tracking messages in the frontend
}
