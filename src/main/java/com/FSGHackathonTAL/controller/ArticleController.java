package com.FSGHackathonTAL.controller;

import com.FSGHackathonTAL.entity.Article;
import com.FSGHackathonTAL.entity.User;
import com.FSGHackathonTAL.service.ArticleService;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import jakarta.servlet.http.HttpSession;

/**
 * Controller xử lý các yêu cầu liên quan đến việc hiển thị bài viết.
 * Bao gồm hiển thị danh sách bài viết (phân trang) và chi tiết của một bài viết cụ thể.
 */
@Controller
public class ArticleController {

    @Autowired
    private ArticleService articleService;

    /**
     * Hiển thị danh sách bài viết với phân trang, sắp xếp theo lượt xem giảm dần.
     * Truyền thông tin người dùng đăng nhập và danh sách bài viết tới view.
     *
     * @param page Số trang hiện tại (mặc định là 0).
     * @param size Kích thước trang (mặc định là 6).
     * @param model Model để truyền dữ liệu tới view.
     * @param session HttpSession để lấy thông tin người dùng nếu cần.
     * @return Tên view "articles".
     */
    // Hiển thị danh sách bài viết với phân trang và sắp xếp theo lượt xem
    @GetMapping("/articles")
    public String showArticleList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "6") int size,
            Model model,
            HttpSession session) {
        try {
            // Lấy thông tin người dùng đang đăng nhập
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            User loggedInUser = null;
            
            if (auth != null && auth.isAuthenticated() && auth.getPrincipal() instanceof User) {
                loggedInUser = (User) auth.getPrincipal();
            } else {
                // Lấy từ session nếu không thể lấy từ Security Context
                loggedInUser = (User) session.getAttribute("loggedInUser");
            }
            
            model.addAttribute("loggedInUser", loggedInUser);
            
            // Đảm bảo page không âm
            page = Math.max(0, page);
            // Đảm bảo size nằm trong khoảng hợp lý
            size = Math.max(1, Math.min(size, 50));
            
            Pageable pageable = PageRequest.of(page, size);
            Page<Article> articlePage = articleService.getArticlesByViewsDesc(pageable);
            
            model.addAttribute("articles", articlePage.getContent());
            model.addAttribute("articlePage", articlePage);
            return "articles";
        } catch (Exception e) {
            // Xử lý lỗi
            model.addAttribute("errorMessage", "Đã xảy ra lỗi khi tải danh sách bài viết: " + e.getMessage());
            model.addAttribute("articles", List.of());  // Thêm danh sách rỗng để tránh lỗi null
            return "articles";
        }
    }

    /**
     * Hiển thị chi tiết một bài viết dựa trên ID.
     * Tăng lượt xem của bài viết và lấy danh sách bài viết ngẫu nhiên khác.
     * Xử lý URL pattern: /articles/{id}
     *
     * @param id ID của bài viết cần hiển thị.
     * @param model Model để truyền dữ liệu tới view.
     * @param session HttpSession để lấy thông tin người dùng nếu cần.
     * @return Tên view "article-detail".
     * @throws IllegalArgumentException Nếu không tìm thấy bài viết với ID cung cấp.
     */
    // Hiển thị chi tiết bài viết theo URL pattern /articles/{id}
    @GetMapping("/articles/{id}")
    public String showArticleDetail(
            @PathVariable("id") Integer id,
            Model model,
            HttpSession session) {
        // Lấy thông tin người dùng đang đăng nhập
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User loggedInUser = null;
        
        if (auth != null && auth.isAuthenticated() && auth.getPrincipal() instanceof User) {
            loggedInUser = (User) auth.getPrincipal();
        } else {
            // Lấy từ session nếu không thể lấy từ Security Context
            loggedInUser = (User) session.getAttribute("loggedInUser");
        }
        
        model.addAttribute("loggedInUser", loggedInUser);
        
        Article article = articleService.getArticleById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy bài viết"));
        articleService.incrementViews(id); // Tăng lượt xem
        List<Article> randomArticles = articleService.getRandomArticles(id, 3); // Lấy 3 bài viết ngẫu nhiên khác

        model.addAttribute("article", article);
        model.addAttribute("randomArticles", randomArticles);
        return "article-detail";
    }
    
    /**
     * Hiển thị chi tiết một bài viết dựa trên ID.
     * Tăng lượt xem của bài viết và lấy danh sách bài viết ngẫu nhiên khác.
     * Xử lý URL pattern: /article/{id} (Cung cấp thêm một URL mapping cho cùng chức năng).
     *
     * @param id ID của bài viết cần hiển thị.
     * @param model Model để truyền dữ liệu tới view.
     * @param session HttpSession để lấy thông tin người dùng nếu cần.
     * @return Tên view "article-detail".
     * @throws IllegalArgumentException Nếu không tìm thấy bài viết với ID cung cấp.
     */
    // Hiển thị chi tiết bài viết theo URL pattern /article/{id}
    @GetMapping("/article/{id}")
    public String viewArticleDetail(
            @PathVariable("id") Integer id,
            Model model,
            HttpSession session) {
        // Lấy thông tin người dùng đang đăng nhập
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User loggedInUser = null;
        
        if (auth != null && auth.isAuthenticated() && auth.getPrincipal() instanceof User) {
            loggedInUser = (User) auth.getPrincipal();
        } else {
            // Lấy từ session nếu không thể lấy từ Security Context
            loggedInUser = (User) session.getAttribute("loggedInUser");
        }
        
        model.addAttribute("loggedInUser", loggedInUser);
        
        Article article = articleService.getArticleById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy bài viết"));
        articleService.incrementViews(id); // Tăng lượt xem
        List<Article> randomArticles = articleService.getRandomArticles(id, 3); // Lấy 3 bài viết ngẫu nhiên khác

        model.addAttribute("article", article);
        model.addAttribute("randomArticles", randomArticles);
        return "article-detail";
    }
}
