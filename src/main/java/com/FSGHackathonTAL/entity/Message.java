package com.FSGHackathonTAL.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entity đại diện cho một tin nhắn (Message) trong một phiên chat.
 * Mỗi tin nhắn thuộc về một ChatSession và có một người gửi (User).
 */
@Entity
@Table(name = "chat_messages")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Message {
    /**
     * ID tự tăng của tin nhắn.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "message_id")
    private Integer messageId;

    /**
     * Phiên chat mà tin nhắn này thuộc về.
     * Liên kết Many-to-One với entity ChatSession.
     * Được loại trừ khỏi phương thức toString() tự động của Lombok.
     */
    @ManyToOne
    @JoinColumn(name = "session_id")
    @ToString.Exclude
    private ChatSession chatSession;

    /**
     * Người gửi tin nhắn (có thể là user hoặc doctor).
     * Liên kết Many-to-One với entity User.
     * Được loại trừ khỏi phương thức toString() tự động của Lombok.
     */
    @ManyToOne
    @JoinColumn(name = "sender_id")
    @ToString.Exclude
    private User sender;

    /**
     * Nội dung của tin nhắn (kiểu TEXT).
     */
    @Column(name = "message_content", nullable = false, columnDefinition = "TEXT")
    private String messageContent;

    /**
     * Thời điểm tin nhắn được gửi, mặc định là thời điểm hiện tại khi tạo.
     */
    @Column(name = "sent_at")
    private LocalDateTime sentAt = LocalDateTime.now();
    
    /**
     * ID định danh phía client, được sử dụng để đồng bộ và tránh trùng lặp trên giao diện.
     * Có thể là null nếu tin nhắn không bắt nguồn từ client có cơ chế này.
     */
    @Column(name = "client_id")
    private String clientId;
} 
