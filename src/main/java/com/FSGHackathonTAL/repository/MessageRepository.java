package com.FSGHackathonTAL.repository;

import com.FSGHackathonTAL.entity.Message;
import com.FSGHackathonTAL.entity.ChatSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface để quản lý các thực thể Message.
 * Cung cấp các phương thức để truy vấn tin nhắn trong các phiên chat.
 */
@Repository
public interface MessageRepository extends JpaRepository<Message, Integer> {
    /**
     * Lấy danh sách tất cả các tin nhắn thuộc về một phiên chat cụ thể.
     * @param chatSession Phiên chat cần lấy tin nhắn.
     * @return Danh sách các Message thuộc phiên chat đó.
     */
    List<Message> findByChatSession(ChatSession chatSession);
    
    /**
     * Lấy danh sách tất cả các tin nhắn thuộc về một phiên chat cụ thể, sắp xếp theo thời gian gửi.
     * Sử dụng truy vấn JPQL để lấy tin nhắn dựa trên ID của phiên chat.
     * @param sessionId ID của phiên chat cần lấy tin nhắn.
     * @return Danh sách các Message thuộc phiên chat đó, đã sắp xếp.
     */
    @Query("SELECT m FROM Message m WHERE m.chatSession.sessionId = :sessionId ORDER BY m.sentAt")
    List<Message> findByChatSessionIdOrderBySentAt(@Param("sessionId") Integer sessionId);
}
