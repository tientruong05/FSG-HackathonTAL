package com.FSGHackathonTAL.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;

/**
 * Entity đại diện cho một cặp câu hỏi và câu trả lời được lưu trữ cho chatbot.
 * Dùng để cung cấp phản hồi cho các câu hỏi thường gặp hoặc đã biết.
 */
@Entity
@Table(name = "chatbot_responses")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatbotResponse {
    /**
     * ID tự tăng của cặp câu hỏi-trả lời.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "response_id")
    private Integer responseId;

    /**
     * Câu hỏi mà người dùng có thể nhập (kiểu TEXT).
     */
    @Column(name = "question", nullable = false, columnDefinition = "TEXT")
    private String question;

    /**
     * Câu trả lời tương ứng của chatbot (kiểu TEXT).
     */
    @Column(name = "answer", nullable = false, columnDefinition = "TEXT")
    private String answer;
}
