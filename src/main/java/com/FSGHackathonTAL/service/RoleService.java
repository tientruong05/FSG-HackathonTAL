package com.FSGHackathonTAL.service;

import com.FSGHackathonTAL.entity.Role;
import com.FSGHackathonTAL.repository.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RoleService {

    @Autowired
    private RoleRepository roleRepository;

    public Role getRoleByName(String roleName) {
        return roleRepository.findByRoleName(roleName)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy vai trò: " + roleName));
    }
}
