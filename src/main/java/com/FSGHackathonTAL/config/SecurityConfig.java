package com.FSGHackathonTAL.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.AuthenticationManager;
import java.util.Collections;

/**
 * Cấu hình bảo mật chính cho ứng dụng sử dụng Spring Security.
 * Kích hoạt bảo mật web và định nghĩa các quy tắc truy cập, quản lý xác thực,
 * và các thành phần bảo mật khác.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private CustomAuthenticationProvider authenticationProvider;

    /**
     * Định nghĩa chuỗi bộ lọc bảo mật (Security Filter Chain).
     * Cấu hình các quy tắc ủy quyền cho các HTTP request, xử lý đăng xuất,
     * và vô hiệu hóa CSRF.
     *
     * @param http Đối tượng HttpSecurity để cấu hình bảo mật.
     * @return SecurityFilterChain đã được cấu hình.
     * @throws Exception Nếu có lỗi trong quá trình cấu hình.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(authz -> authz
                // 1. Permit All (Cho phép truy cập không cần xác thực)
                .requestMatchers(
                    // Tài nguyên tĩnh
                    "/css/**", "/js/**", "/images/**", "/uploads/**", "/sounds/**", "/music/**",
                    "/favicon.ico", "/webjars/**",
                    // Endpoint đăng nhập/đăng ký
                    "/user/login", "/user/register",
                    // WebSocket
                    "/ws-chat/**", "/ws-chat-doctor/**", "/app/**", "/topic/**",
                    // Trang công khai
                    "/", "/home",
                    "/articles", "/articles/**", "/article/**", // Trang danh sách và chi tiết bài viết
                    "/popular-doctors", "/api/popular-doctors", // Trang bác sĩ nổi bật và API
                    "/chill-mode" // Trang thư giãn
                ).permitAll()

                // 2. Quy tắc truy cập theo vai trò cụ thể
                // Admin: Toàn bộ khu vực /admin/**
                .requestMatchers("/admin/**").hasAuthority("admin")

                // Doctor: CHỈ quản lý chat và trạng thái online
                .requestMatchers(
                    "/doctor/chats", "/doctor/chats/**",
                    "/doctor/toggle-online",
                    "/api/doctor/status" // API lấy trạng thái bác sĩ
                    // Loại bỏ: /select-doctor, /doctor-selection vì user cũng cần
                ).hasAuthority("doctor")

                // User: Các tính năng dành riêng cho người dùng đã đăng nhập
                .requestMatchers(
                    "/chatbot", "/api/chatbot/**", // Chatbot
                    "/chat/**", "/user/send-message", // Chat với bác sĩ (user bắt đầu)
                    "/select-doctor", "/doctor-selection-content", // Chọn bác sĩ
                    "/favorite-doctors/**", "/like-doctor/**", // Bác sĩ yêu thích
                    "/check-active-chat", "/end-chat/**", "/user-end-chat/**", "/chat-history/**" // Các API/endpoint chat khác của user
                ).hasAuthority("user")

                // 3. Truy cập chung cần xác thực (Profile, Logout) - Bất kỳ ai đăng nhập
                .requestMatchers("/logout").authenticated()
                .requestMatchers("/profile", "/user/update-profile", "/user/update-password").hasAnyAuthority("user", "doctor")

                // 4. Quy tắc cuối cùng: Từ chối mọi request không khớp các quy tắc trên
                .anyRequest().denyAll()
            )
            // QUAN TRỌNG: Vô hiệu hóa bảo mật mặc định cho form đăng nhập
            .formLogin(login -> login.disable())
            .logout(logout -> logout
                .logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
                .logoutSuccessUrl("/home")
                .permitAll()
            );
        
        return http.build();
    }
    
    /**
     * Cấu hình AuthenticationManager sử dụng CustomAuthenticationProvider.
     * AuthenticationManager chịu trách nhiệm xử lý các yêu cầu xác thực.
     * @return AuthenticationManager đã được cấu hình.
     */
    @Bean
    public AuthenticationManager authenticationManager() {
        return new ProviderManager(Collections.singletonList(authenticationProvider));
    }
    
    /**
     * Cấu hình SecurityContextRepository để lưu trữ SecurityContext giữa các request.
     * Sử dụng HttpSessionSecurityContextRepository để lưu context trong session HTTP.
     * @return SecurityContextRepository đã được cấu hình.
     */
    @Bean
    public SecurityContextRepository securityContextRepository() {
        return new HttpSessionSecurityContextRepository();
    }
    
    /**
     * Cấu hình PasswordEncoder.
     * Sử dụng NoOpPasswordEncoder cho mục đích đơn giản hóa (KHÔNG khuyến nghị cho môi trường production).
     * Trong môi trường thực tế, nên sử dụng một bộ mã hóa mật khẩu mạnh như BCryptPasswordEncoder.
     * @return PasswordEncoder (hiện tại là NoOpPasswordEncoder).
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        // For simplicity, we're using the existing password storage mechanism
        // In a production environment, you should use a proper password encoder like BCrypt
        return NoOpPasswordEncoder.getInstance();
    }
}