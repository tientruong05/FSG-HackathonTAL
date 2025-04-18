package com.FSGHackathonTAL.controller;

import com.FSGHackathonTAL.entity.Mood;
import com.FSGHackathonTAL.entity.User;
import com.FSGHackathonTAL.service.MoodService;
import com.FSGHackathonTAL.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controller xử lý các chức năng liên quan đến nhật ký tâm trạng của người dùng.
 */
@Controller
public class MoodController {

    @Autowired
    private MoodService moodService;

    @Autowired
    private UserService userService;

    /**
     * Hiển thị form nhập tâm trạng.
     */
    @GetMapping("/mood")
    public String showMoodForm(Model model, HttpSession session) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null) {
            return "redirect:/home";
        }
        
        model.addAttribute("loggedInUser", loggedInUser);
        return "mood-form";
    }

    /**
     * Hiển thị lịch sử nhật ký tâm trạng của người dùng.
     */
    @GetMapping("/mood/history")
    public String showMoodHistory(Model model, HttpSession session) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null) {
            return "redirect:/home";
        }
        
        List<Mood> moodEntries = moodService.getMoodsByUser(loggedInUser);
        model.addAttribute("moodEntries", moodEntries);
        model.addAttribute("loggedInUser", loggedInUser);
        return "mood-history";
    }

    /**
     * Lưu một entry tâm trạng mới.
     */
    @PostMapping("/mood/save")
    public String saveMood(
            @RequestParam("moodLevel") String moodLevel,
            @RequestParam("note") String note,
            HttpSession session,
            RedirectAttributes redirectAttributes,
            Model model) {
        
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null) {
            return "redirect:/home";
        }
        
        try {
            // Sử dụng thời gian hiện tại
            Date createdAt = new Date();
            
            // Tạo và lưu entity Mood
            Mood mood = new Mood();
            mood.setMood(moodLevel);
            mood.setReason(note);
            mood.setCreatedAt(createdAt);
            mood.setUser(loggedInUser);
            
            moodService.saveMood(mood);
            
            // Hiển thị thông báo thành công trên trang hiện tại
            redirectAttributes.addFlashAttribute("successMessage", "Đã lưu tâm trạng thành công!");
            return "redirect:/mood";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Đã xảy ra lỗi: " + e.getMessage());
            return "redirect:/mood";
        }
    }

    /**
     * API lấy lịch sử tâm trạng của một người dùng cụ thể (chỉ bác sĩ mới được sử dụng).
     */
    @GetMapping("/api/mood/user/{userId}")
    @ResponseBody
    public ResponseEntity<?> getUserMoodHistory(
            @PathVariable Integer userId,
            HttpSession session) {
        
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null || !"doctor".equals(loggedInUser.getRole().getRoleName())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Không có quyền truy cập"));
        }
        
        try {
            List<Mood> moodEntries = moodService.getMoodsByUserId(userId);
            return ResponseEntity.ok(moodEntries);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Lỗi khi lấy dữ liệu tâm trạng: " + e.getMessage()));
        }
    }

    /**
     * Xóa một entry tâm trạng.
     */
    @PostMapping("/mood/delete/{entryId}")
    public String deleteMood(
            @PathVariable Integer entryId,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null) {
            return "redirect:/home";
        }
        
        try {
            // Kiểm tra xem entry có thuộc về người dùng hiện tại không
            Mood mood = moodService.getMoodById(entryId)
                    .orElseThrow(() -> new Exception("Không tìm thấy entry tâm trạng"));
            
            if (!mood.getUser().getUserId().equals(loggedInUser.getUserId())) {
                redirectAttributes.addFlashAttribute("errorMessage", "Bạn không có quyền xóa entry này!");
                return "redirect:/mood/history";
            }
            
            moodService.deleteMood(entryId);
            redirectAttributes.addFlashAttribute("successMessage", "Đã xóa entry tâm trạng thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi khi xóa entry: " + e.getMessage());
        }
        
        return "redirect:/mood/history";
    }
} 