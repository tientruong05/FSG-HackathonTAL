package com.FSGHackathonTAL.dto;

import java.time.LocalDateTime;

/**
 * Data Transfer Object (DTO) đại diện cho một tin nhắn trong phiên chat giữa người dùng và bác sĩ.
 * Được sử dụng để truyền dữ liệu tin nhắn qua WebSocket và API.
 */
public class MessageDTO {
    /**
     * ID của phiên chat mà tin nhắn này thuộc về.
     */
    private Integer sessionId;

    /**
     * ID của người gửi tin nhắn (có thể là người dùng hoặc bác sĩ).
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
     * ID định danh phía client, dùng để tránh hiển thị tin nhắn trùng lặp trên giao diện.
     */
    private String _clientId;

    /**
     * Constructor mặc định.
     * Cần thiết cho việc deserialize JSON.
     */
    public MessageDTO() {
        // Default constructor needed for JSON deserialization
    }

    /**
     * Constructor với các tham số cơ bản của tin nhắn.
     * @param sessionId ID phiên chat.
     * @param senderId ID người gửi.
     * @param messageContent Nội dung tin nhắn.
     * @param sentAt Thời điểm gửi.
     */
    public MessageDTO(Integer sessionId, Integer senderId, String messageContent, LocalDateTime sentAt) {
        this.sessionId = sessionId;
        this.senderId = senderId;
        this.messageContent = messageContent;
        this.sentAt = sentAt;
    }

    // Getters and setters

    /**
     * Lấy ID phiên chat.
     * @return ID phiên chat.
     */
    public Integer getSessionId() { return sessionId; }

    /**
     * Thiết lập ID phiên chat.
     * @param sessionId ID phiên chat mới.
     */
    public void setSessionId(Integer sessionId) { this.sessionId = sessionId; }

    /**
     * Lấy ID người gửi.
     * @return ID người gửi.
     */
    public Integer getSenderId() { return senderId; }

    /**
     * Thiết lập ID người gửi.
     * @param senderId ID người gửi mới.
     */
    public void setSenderId(Integer senderId) { this.senderId = senderId; }

    /**
     * Lấy nội dung tin nhắn.
     * @return Nội dung tin nhắn.
     */
    public String getMessageContent() { return messageContent; }

    /**
     * Thiết lập nội dung tin nhắn.
     * @param messageContent Nội dung tin nhắn mới.
     */
    public void setMessageContent(String messageContent) { this.messageContent = messageContent; }

    /**
     * Lấy thời điểm gửi tin nhắn.
     * @return Thời điểm gửi.
     */
    public LocalDateTime getSentAt() { return sentAt; }

    /**
     * Thiết lập thời điểm gửi tin nhắn.
     * @param sentAt Thời điểm gửi mới.
     */
    public void setSentAt(LocalDateTime sentAt) { this.sentAt = sentAt; }

    /**
     * Lấy ID định danh phía client.
     * @return ID client.
     */
    public String get_clientId() { return _clientId; }

    /**
     * Thiết lập ID định danh phía client.
     * @param _clientId ID client mới.
     */
    public void set_clientId(String _clientId) { this._clientId = _clientId; }
}
