package com.FSGHackathonTAL.service;

import com.FSGHackathonTAL.entity.Mood;
import com.FSGHackathonTAL.entity.User;
import com.FSGHackathonTAL.repository.MoodRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * Service xử lý các thao tác liên quan đến nhật ký tâm trạng của người dùng.
 */
@Service
public class MoodService {

    @Autowired
    private MoodRepository moodRepository;

    /**
     * Lưu một entry tâm trạng mới cho người dùng.
     * 
     * @param mood Đối tượng tâm trạng cần lưu
     * @return Đối tượng tâm trạng đã được lưu
     */
    @Transactional
    public Mood saveMood(Mood mood) {
        if (mood.getCreatedAt() == null) {
            mood.setCreatedAt(new Date());
        }
        return moodRepository.save(mood);
    }

    /**
     * Lấy danh sách tâm trạng của một người dùng cụ thể.
     * 
     * @param user Người dùng cần lấy nhật ký tâm trạng
     * @return Danh sách các entry tâm trạng của người dùng, sắp xếp theo thời gian giảm dần
     */
    public List<Mood> getMoodsByUser(User user) {
        return moodRepository.findByUserOrderByCreatedAtDesc(user);
    }

    /**
     * Lấy danh sách tâm trạng của một người dùng dựa vào ID.
     * 
     * @param userId ID của người dùng cần lấy nhật ký tâm trạng
     * @return Danh sách các entry tâm trạng của người dùng, sắp xếp theo thời gian giảm dần
     */
    public List<Mood> getMoodsByUserId(Integer userId) {
        return moodRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    /**
     * Lấy một entry tâm trạng cụ thể theo ID.
     * 
     * @param entryId ID của entry tâm trạng cần lấy
     * @return Optional chứa đối tượng Mood nếu tìm thấy, ngược lại là Optional rỗng
     */
    public Optional<Mood> getMoodById(Integer entryId) {
        return moodRepository.findById(entryId);
    }

    /**
     * Xóa một entry tâm trạng.
     * 
     * @param entryId ID của entry tâm trạng cần xóa
     */
    @Transactional
    public void deleteMood(Integer entryId) {
        moodRepository.deleteById(entryId);
    }
} 