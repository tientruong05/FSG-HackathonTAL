package com.FSGHackathonTAL.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import jakarta.persistence.*;
import java.util.List;

/**
 * Entity đại diện cho một người dùng (User) trong hệ thống.
 * Người dùng có thể có vai trò là "user", "doctor", hoặc "admin".
 * Lưu trữ thông tin cá nhân, thông tin đăng nhập, trạng thái và các mối quan hệ.
 */
@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
    /**
     * ID tự tăng của người dùng.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Integer userId;

    /**
     * Vai trò của người dùng.
     * Liên kết Many-to-One với entity Role (Eager loaded).
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "role_id")
    private Role role;

    /**
     * Họ và tên đầy đủ của người dùng.
     */
    @Column(name = "full_name", nullable = false)
    private String fullName;

    /**
     * Địa chỉ email của người dùng, dùng để đăng nhập.
     * Là duy nhất và không được để trống.
     */
    @Column(name = "email", nullable = false, unique = true)
    private String email;

    /**
     * Mật khẩu của người dùng (đã được mã hóa).
     * Không được để trống.
     */
    @Column(name = "password", nullable = false)
    private String password;

    /**
     * Số điện thoại của người dùng (không bắt buộc).
     */
    @Column(name = "phone")
    private String phone;

    /**
     * Đường dẫn đến ảnh đại diện của người dùng.
     */
    @Column(name = "image", nullable = false)
    private String image;

    /**
     * Trạng thái online/offline của người dùng, mặc định là false (offline).
     */
    @Column(name = "is_online")
    private Boolean isOnline = false;

    /**
     * Trạng thái kích hoạt tài khoản, mặc định là true (active).
     */
    @Column(name = "is_active")
    private Boolean isActive = true;

    /**
     * Số lượt thích (dành cho bác sĩ), mặc định là 0.
     */
    @Column(name = "likes")
    private Integer likes = 0;

    /**
     * Danh sách các phiên chat mà người dùng này tham gia với tư cách là người dùng thông thường.
     * Liên kết One-to-Many với entity ChatSession (lazy loaded).
     * Được loại trừ khỏi phương thức toString() tự động của Lombok.
     */
    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    @ToString.Exclude
    private List<ChatSession> chatSessionsAsUser;

    /**
     * Danh sách các phiên chat mà người dùng này tham gia với tư cách là bác sĩ.
     * Liên kết One-to-Many với entity ChatSession (lazy loaded).
     * Được loại trừ khỏi phương thức toString() tự động của Lombok.
     */
    @OneToMany(mappedBy = "doctor", fetch = FetchType.LAZY)
    @ToString.Exclude
    private List<ChatSession> chatSessionsAsDoctor;

    /**
     * Danh sách các bài viết mà người dùng này (với vai trò admin) đã tạo.
     * Liên kết One-to-Many với entity Article (lazy loaded).
     * Được loại trừ khỏi phương thức toString() tự động của Lombok.
     */
    @OneToMany(mappedBy = "admin", fetch = FetchType.LAZY)
    @ToString.Exclude
    private List<Article> articles;
    
    /**
     * Ghi đè phương thức toString() để hiển thị thông tin cơ bản của User,
     * bao gồm ID, tên, email, vai trò, trạng thái online và active.
     * Tránh việc load các danh sách liên quan khi không cần thiết.
     * @return Chuỗi đại diện cho User.
     */
    @Override
    public String toString() {
        return "User(userId=" + userId + 
               ", fullName=" + fullName + 
               ", email=" + email + 
               ", role=" + (role != null ? role.getRoleName() : "null") + 
               ", isOnline=" + isOnline + 
               ", isActive=" + isActive + ")";
    }
}
