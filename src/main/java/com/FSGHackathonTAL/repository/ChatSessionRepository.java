package com.FSGHackathonTAL.repository;

import com.FSGHackathonTAL.entity.ChatSession;
import com.FSGHackathonTAL.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface để quản lý các thực thể ChatSession.
 * Cung cấp các phương thức để truy vấn lịch sử và trạng thái phiên chat.
 */
public interface ChatSessionRepository extends JpaRepository<ChatSession, Integer> {
    /**
     * Lấy danh sách tất cả các phiên chat của một người dùng cụ thể.
     * @param user Người dùng cần lấy lịch sử chat.
     * @return Danh sách các ChatSession của người dùng đó.
     */
    List<ChatSession> findByUser(User user);

    /**
     * Lấy danh sách tất cả các phiên chat của một bác sĩ cụ thể.
     * @param doctor Bác sĩ cần lấy lịch sử chat.
     * @return Danh sách các ChatSession của bác sĩ đó.
     */
    List<ChatSession> findByDoctor(User doctor);

    /**
     * Lấy danh sách các phiên chat của một bác sĩ cụ thể, có phân trang.
     * @param doctor Bác sĩ cần lấy lịch sử chat.
     * @param pageable Thông tin phân trang.
     * @return Trang (Page) chứa các ChatSession của bác sĩ đó.
     */
    Page<ChatSession> findByDoctor(User doctor, Pageable pageable);

    /**
     * Lấy danh sách các phiên chat đang hoạt động (chưa kết thúc) của một bác sĩ cụ thể.
     * @param doctor Bác sĩ cần kiểm tra.
     * @return Danh sách các ChatSession đang hoạt động của bác sĩ đó.
     */
    List<ChatSession> findByDoctorAndEndTimeIsNull(User doctor);

    /**
     * Lấy danh sách các phiên chat đang hoạt động (chưa kết thúc) của một người dùng cụ thể.
     * @param user Người dùng cần kiểm tra.
     * @return Danh sách các ChatSession đang hoạt động của người dùng đó.
     */
    List<ChatSession> findByUserAndEndTimeIsNull(User user);

    /**
     * Tìm phiên chat đang hoạt động gần nhất (mới nhất) của một người dùng.
     * @param user Người dùng cần kiểm tra.
     * @return Optional chứa ChatSession đang hoạt động mới nhất nếu có, ngược lại là Optional rỗng.
     */
    Optional<ChatSession> findFirstByUserAndEndTimeIsNullOrderByStartTimeDesc(User user);

    /**
     * Tìm phiên chat đang hoạt động gần nhất (mới nhất) của một bác sĩ.
     * @param doctor Bác sĩ cần kiểm tra.
     * @return Optional chứa ChatSession đang hoạt động mới nhất nếu có, ngược lại là Optional rỗng.
     */
    Optional<ChatSession> findFirstByDoctorAndEndTimeIsNullOrderByStartTimeDesc(User doctor);
}
