package com.FSGHackathonTAL.repository;

import com.FSGHackathonTAL.entity.Article;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository interface để quản lý các thực thể Article.
 * Cung cấp các phương thức CRUD cơ bản cho Article.
 */
public interface ArticleRepository extends JpaRepository<Article, Integer> {
}
