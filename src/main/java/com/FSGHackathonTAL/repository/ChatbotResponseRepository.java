package com.FSGHackathonTAL.repository;

import com.FSGHackathonTAL.entity.ChatbotResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

/**
 * Repository interface để quản lý các thực thể ChatbotResponse.
 * Cung cấp các phương thức để truy vấn câu trả lời của chatbot.
 */
public interface ChatbotResponseRepository extends JpaRepository<ChatbotResponse, Integer> {
    /**
     * Tìm câu trả lời chatbot dựa trên một phần của câu hỏi (không phân biệt chữ hoa/thường).
     * @param question Phần của câu hỏi cần tìm kiếm.
     * @return Optional chứa ChatbotResponse nếu tìm thấy, ngược lại là Optional rỗng.
     */
    @Query("SELECT cr FROM ChatbotResponse cr WHERE LOWER(cr.question) LIKE LOWER(CONCAT('%', :question, '%'))")
    Optional<ChatbotResponse> findByQuestionContaining(String question);
}