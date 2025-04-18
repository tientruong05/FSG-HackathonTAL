package com.FSGHackathonTAL.repository;

import com.FSGHackathonTAL.entity.Mood;
import com.FSGHackathonTAL.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface để quản lý các thực thể Mood.
 * Cung cấp các phương thức để truy vấn nhật ký tâm trạng của người dùng.
 */
@Repository
public interface MoodRepository extends JpaRepository<Mood, Integer> {
    /**
     * Lấy danh sách tất cả các entry tâm trạng của một người dùng cụ thể.
     * @param user Người dùng cần lấy nhật ký tâm trạng.
     * @return Danh sách các Mood thuộc người dùng đó, sắp xếp theo thời gian tạo giảm dần.
     */
    List<Mood> findByUserOrderByCreatedAtDesc(User user);
    
    /**
     * Lấy danh sách tâm trạng của một người dùng cụ thể dựa trên ID.
     * @param userId ID của người dùng cần lấy nhật ký tâm trạng.
     * @return Danh sách các Mood thuộc người dùng đó, sắp xếp theo thời gian tạo giảm dần.
     */
    @Query("SELECT m FROM Mood m WHERE m.user.userId = :userId ORDER BY m.createdAt DESC")
    List<Mood> findByUserIdOrderByCreatedAtDesc(@Param("userId") Integer userId);
} 