package com.FSGHackathonTAL.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entity đại diện cho một phiên chat (Chat Session) giữa một người dùng và một bác sĩ.
 * Lưu trữ thông tin về người tham gia, thời gian và ghi chú (nếu có).
 */
@Entity
@Table(name = "chat_sessions")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatSession {
    /**
     * ID tự tăng của phiên chat.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "session_id")
    private Integer sessionId;

    /**
     * Người dùng tham gia phiên chat.
     * Liên kết Many-to-One với entity User.
     * Được loại trừ khỏi phương thức toString() tự động của Lombok.
     */
    @ManyToOne
    @JoinColumn(name = "user_id")
    @ToString.Exclude
    private User user;

    /**
     * Bác sĩ tham gia phiên chat.
     * Liên kết Many-to-One với entity User.
     * Được loại trừ khỏi phương thức toString() tự động của Lombok.
     */
    @ManyToOne
    @JoinColumn(name = "doctor_id")
    @ToString.Exclude
    private User doctor;

    /**
     * Thời điểm bắt đầu phiên chat, mặc định là thời điểm hiện tại khi tạo.
     */
    @Column(name = "start_time")
    private LocalDateTime startTime = LocalDateTime.now();

    /**
     * Thời điểm kết thúc phiên chat. Null nếu phiên chat chưa kết thúc.
     */
    @Column(name = "end_time")
    private LocalDateTime endTime;

    /**
     * Ghi chú của bác sĩ về phiên chat (kiểu TEXT). Có thể là null.
     */
    @Column(name = "doctor_notes", columnDefinition = "TEXT")
    private String doctorNotes;
}
