package com.FSGHackathonTAL.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.AuthenticationManager;
import java.util.Collections;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;

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
            .csrf(AbstractHttpConfigurer::disable)
            .authorizeHttpRequests(auth -> auth
                // URLs cho tài nguyên tĩnh - cho phép truy cập không cần xác thực
                .requestMatchers("/uploads/**", "/css/**", "/js/**", "/images/**", "/static/**", "/fonts/**", "/favicon.ico").permitAll()
                // URLs công khai - cho phép truy cập không cần xác thực
                .requestMatchers("/", "/home", "/articles", "/articles/**", "/popular-doctors", "/chill-mode").permitAll()
                .requestMatchers("/ws/**", "/topic/**", "/app/**").permitAll()
                .requestMatchers("/error").permitAll()
                .requestMatchers("/user/login", "/user/register").permitAll()
                .requestMatchers("/webjars/**").permitAll()
                // URLs cho Admin
                .requestMatchers("/admin/**").hasAuthority("admin")
                // URLs cho Doctor
                .requestMatchers("/doctor/**").hasAuthority("doctor")
                // URLs cho cả Admin và Doctor
                .requestMatchers("/manage/**").hasAnyAuthority("admin", "doctor")
                // URLs cho User đã đăng nhập
                .requestMatchers("/chatbot", "/chat/**", "/profile/**").hasAuthority("user")
                // URLs cho bất kỳ người dùng đã đăng nhập nào
                .requestMatchers("/dashboard").authenticated()
                // Yêu cầu xác thực cho tất cả các URL còn lại
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/home")
                .usernameParameter("email")
                .passwordParameter("password")
                .loginProcessingUrl("/user/login-process")
                .defaultSuccessUrl("/home", true)
                .failureUrl("/home?error=true")
                .permitAll()
            )
            .logout(logout -> logout
                .logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
                .logoutSuccessUrl("/home")
                .invalidateHttpSession(true)
                .clearAuthentication(true)
                .deleteCookies("JSESSIONID")
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
     * Sử dụng BCryptPasswordEncoder cho mục đích mật khẩu mạnh.
     * @return PasswordEncoder (BCryptPasswordEncoder).
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}