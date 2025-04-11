package com.FSGHackathonTAL.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import jakarta.persistence.*;
import java.util.List;

/**
 * Entity đại diện cho một vai trò (Role) trong hệ thống.
 * Ví dụ: "user", "doctor", "admin".
 */
@Entity
@Table(name = "roles")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Role {
    /**
     * ID tự tăng của vai trò.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "role_id")
    private Integer roleId;

    /**
     * Tên của vai trò (ví dụ: "admin", "user", "doctor").
     * Tên vai trò là duy nhất và không được để trống.
     */
    @Column(name = "role_name", nullable = false, unique = true)
    private String roleName;

    /**
     * Danh sách người dùng thuộc vai trò này.
     * Liên kết One-to-Many với entity User (lazy loaded).
     * Được loại trừ khỏi phương thức toString() tự động của Lombok để tránh lỗi.
     */
    @OneToMany(mappedBy = "role", fetch = FetchType.LAZY)
    @ToString.Exclude
    private List<User> users;
    
    /**
     * Ghi đè phương thức toString() để chỉ hiển thị thông tin cơ bản của Role,
     * tránh việc load danh sách Users khi không cần thiết.
     * @return Chuỗi đại diện cho Role (chỉ gồm roleId và roleName).
     */
    @Override
    public String toString() {
        return "Role(roleId=" + roleId + ", roleName=" + roleName + ")";
    }
}
