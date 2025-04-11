package com.FSGHackathonTAL.repository;

import com.FSGHackathonTAL.entity.Role;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository interface để quản lý các thực thể Role.
 * Cung cấp các phương thức CRUD cơ bản và phương thức tùy chỉnh để tìm Role theo tên.
 */
public interface RoleRepository extends JpaRepository<Role, Integer> {
    
    /**
     * Tìm kiếm một Role dựa trên tên vai trò.
     * @param roleName Tên vai trò cần tìm.
     * @return Optional chứa Role nếu tìm thấy, ngược lại là Optional rỗng.
     */
    Optional<Role> findByRoleName(String roleName);
}