package com.FSGHackathonTAL.service;

import com.FSGHackathonTAL.entity.Article;
import com.FSGHackathonTAL.entity.User;
import com.FSGHackathonTAL.repository.ArticleRepository;
import jakarta.validation.constraints.NotBlank;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Random;

@Service
public class ArticleService {

    @Autowired
    private ArticleRepository articleRepository;

    public List<Article> getAllArticles() {
        return articleRepository.findAll();
    }

    public Optional<Article> getArticleById(Integer articleId) {
        return articleRepository.findById(articleId);
    }

    public Article createArticle(User admin,
                                 @NotBlank(message = "Tiêu đề là bắt buộc") String title,
                                 @NotBlank(message = "Ảnh là bắt buộc") String image,
                                 @NotBlank(message = "Nội dung là bắt buộc") String content) {
        Article article = new Article();
        article.setAdmin(admin);
        article.setTitle(title);
        article.setImage(image);
        article.setContent(content);
        article.setViews(0);
        return articleRepository.save(article);
    }

    public Article updateArticle(Integer articleId, String title, String image, String content) {
        Article article = articleRepository.findById(articleId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy bài viết"));
        article.setTitle(title);
        article.setImage(image);
        article.setContent(content);
        return articleRepository.save(article);
    }

    public void deleteArticle(Integer articleId) {
        if (!articleRepository.existsById(articleId)) {
            throw new IllegalArgumentException("Không tìm thấy bài viết");
        }
        articleRepository.deleteById(articleId);
    }

    public Page<Article> getAllArticlesPaged(Pageable pageable) {
        return articleRepository.findAll(pageable);
    }

    // Lấy danh sách bài viết sắp xếp theo lượt xem từ cao xuống thấp
    public Page<Article> getArticlesByViewsDesc(Pageable pageable) {
        return articleRepository.findAll(PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by("views").descending()));
    }

    // Lấy danh sách bài viết ngẫu nhiên (trừ bài viết hiện tại)
    public List<Article> getRandomArticles(Integer excludeId, int limit) {
        List<Article> allArticles = articleRepository.findAll();
        allArticles.removeIf(article -> article.getArticleId().equals(excludeId)); // Loại bài viết hiện tại
        Random rand = new Random();
        return allArticles.stream()
                .sorted((a, b) -> rand.nextInt(3) - 1) // Sắp xếp ngẫu nhiên
                .limit(limit)
                .toList();
    }

    // Tăng lượt xem khi xem chi tiết bài viết
    public void incrementViews(Integer articleId) {
        Article article = articleRepository.findById(articleId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy bài viết"));
        article.setViews(article.getViews() + 1);
        articleRepository.save(article);
    }

    // Lấy các bài viết mới nhất
    public List<Article> getRecentArticles(int limit) {
        Pageable pageable = PageRequest.of(0, limit, Sort.by("createdAt").descending());
        return articleRepository.findAll(pageable).getContent();
    }
}
