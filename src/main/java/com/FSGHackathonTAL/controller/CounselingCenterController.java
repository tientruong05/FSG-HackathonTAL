package com.FSGHackathonTAL.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import com.FSGHackathonTAL.entity.User;
import com.FSGHackathonTAL.service.UserService;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/centers")
@RequiredArgsConstructor
public class CounselingCenterController {

    private final UserService userService;

    @GetMapping("")
    public String centerSearchPage(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String city,
            Model model,
            HttpSession session) {
        
        // Kiểm tra người dùng đã đăng nhập chưa từ SecurityContext trước
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User loggedInUser = null;
        
        if (auth != null && auth.isAuthenticated() && auth.getPrincipal() instanceof User) {
            loggedInUser = (User) auth.getPrincipal();
            model.addAttribute("loggedInUser", loggedInUser);
            session.setAttribute("loggedInUser", loggedInUser);
        } else {
            // Nếu không tìm thấy trong SecurityContext, thử lấy từ session
            loggedInUser = (User) session.getAttribute("loggedInUser");
            if (loggedInUser != null) {
                model.addAttribute("loggedInUser", loggedInUser);
            } else {
                // Nếu vẫn không có, thử lấy từ userId trong session
                Integer userId = (Integer) session.getAttribute("userId");
                if (userId != null) {
                    loggedInUser = userService.getUserById(userId).orElse(null);
                    if (loggedInUser != null) {
                        model.addAttribute("loggedInUser", loggedInUser);
                        session.setAttribute("loggedInUser", loggedInUser);
                    }
                }
            }
        }
        
        // Truyền các tham số tìm kiếm vào model nếu có
        if (name != null && !name.isEmpty()) {
            model.addAttribute("searchName", name);
        }
        
        if (city != null && !city.isEmpty()) {
            model.addAttribute("searchCity", city);
        }
        
        return "centers";
    }
} 