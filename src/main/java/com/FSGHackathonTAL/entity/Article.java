package com.FSGHackathonTAL.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entity đại diện cho một bài viết (article) trong hệ thống.
 * Lưu trữ thông tin về tiêu đề, nội dung, tác giả (admin), lượt xem, ảnh và ngày tạo.
 */
@Entity
@Table(name = "articles")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Article {
    /**
     * ID tự tăng của bài viết.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "article_id")
    private Integer articleId;

    /**
     * Người quản trị (admin) đã tạo bài viết.
     * Liên kết Many-to-One với entity User.
     */
    @ManyToOne
    @JoinColumn(name = "admin_id")
    private User admin;

    /**
     * Tiêu đề của bài viết.
     */
    @Column(name = "title", nullable = false)
    private String title;

    /**
     * Đường dẫn đến hình ảnh đại diện của bài viết.
     */
    @Column(name = "image", nullable = false)
    private String image;

    /**
     * Nội dung chi tiết của bài viết (kiểu TEXT).
     */
    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    /**
     * Số lượt xem bài viết, mặc định là 0.
     */
    @Column(name = "views")
    private Integer views = 0;

    /**
     * Thời điểm bài viết được tạo, mặc định là thời điểm hiện tại.
     */
    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
}