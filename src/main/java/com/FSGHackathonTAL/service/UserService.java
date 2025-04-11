package com.FSGHackathonTAL.service;

import com.FSGHackathonTAL.entity.Role;
import com.FSGHackathonTAL.entity.User;
import com.FSGHackathonTAL.repository.RoleRepository;
import com.FSGHackathonTAL.repository.UserRepository;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    public User login(String email, String password) {
        return userRepository.findByEmailAndPassword(email, password)
                .orElseThrow(() -> new IllegalArgumentException("Email hoặc mật khẩu không đúng!"));
    }

    public User saveUser(User user) {
        return userRepository.save(user);
    }
    
    public Optional<User> getUserById(Integer userId) {
        return userRepository.findById(userId);
    }

    public User register(@NotBlank(message = "Họ tên là bắt buộc") String fullName,
                         @NotBlank(message = "Email là bắt buộc") @Email String email,
                         @NotBlank(message = "Mật khẩu là bắt buộc") String password,
                         @NotBlank(message = "Xác nhận mật khẩu là bắt buộc") String confirmPassword) {
        if (!password.equals(confirmPassword)) {
            throw new IllegalArgumentException("Mật khẩu xác nhận không khớp!");
        }
        if (userRepository.findByEmail(email).isPresent()) {
            throw new IllegalArgumentException("Email đã được sử dụng!");
        }

        User user = new User();
        user.setFullName(fullName);
        user.setEmail(email);
        user.setPassword(password);
        user.setImage("/uploads/default.png");
        user.setRole(roleRepository.findByRoleName("user")
                .orElseThrow(() -> new IllegalArgumentException("Vai trò 'user' không tồn tại")));
        return userRepository.save(user);
    }

    public User updateProfile(Integer userId,
                              @NotBlank(message = "Họ tên là bắt buộc") String fullName,
                              String phone,
                              @NotBlank(message = "Email là bắt buộc") @Email(message = "Email không hợp lệ") String email) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng"));
        if (!user.getEmail().equals(email) && userRepository.findByEmail(email).isPresent()) {
            throw new IllegalArgumentException("Email đã được sử dụng bởi người khác");
        }
        user.setFullName(fullName);
        user.setPhone(phone);
        user.setEmail(email);
        return userRepository.save(user);
    }

    public List<User> getOnlineDoctors() {
        return userRepository.findOnlineDoctors();
    }

    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng với email: " + email));
    }

    public List<User> getUsersByRole(String roleName) {
        return userRepository.findByRoleRoleName(roleName);
    }

    public Page<User> getUsersByRolePaged(String roleName, Pageable pageable) {
        return userRepository.findByRoleRoleName(roleName, pageable);
    }

    public void deleteUser(Integer userId) {
        if (!userRepository.existsById(userId)) {
            throw new IllegalArgumentException("Không tìm thấy người dùng");
        }
        userRepository.deleteById(userId);
    }

    /**
     * Kiểm tra email đã tồn tại hay chưa
     * @param email Email cần kiểm tra
     * @param userId ID của user đang cập nhật (null nếu tạo mới)
     * @return true nếu email đã tồn tại
     */
    public boolean isEmailExist(String email, Integer userId) {
        Optional<User> existingUser = userRepository.findByEmail(email);
        if (existingUser.isEmpty()) {
            return false;
        }
        
        // Nếu đang cập nhật và email thuộc về chính user đó
        return userId == null || !existingUser.get().getUserId().equals(userId);
    }
    
    /**
     * Kiểm tra số điện thoại đã tồn tại hay chưa
     * @param phone Số điện thoại cần kiểm tra
     * @param userId ID của user đang cập nhật (null nếu tạo mới)
     * @return true nếu số điện thoại đã tồn tại
     */
    public boolean isPhoneExist(String phone, Integer userId) {
        if (phone == null || phone.trim().isEmpty()) {
            return false; // Không kiểm tra nếu phone trống
        }
        
        List<User> allUsers = userRepository.findAll();
        return allUsers.stream()
                .filter(u -> phone.equals(u.getPhone()))
                .anyMatch(u -> userId == null || !u.getUserId().equals(userId));
    }
    
    /**
     * Kiểm tra mật khẩu hợp lệ (>= 8 ký tự, không chứa khoảng trắng)
     * @param password Mật khẩu cần kiểm tra
     * @return true nếu mật khẩu hợp lệ
     */
    public boolean isValidPassword(String password) {
        return password != null && password.length() >= 8 && !password.contains(" ");
    }
    
    /**
     * Validate thông tin người dùng khi thêm mới hoặc cập nhật
     * @param fullName Họ tên
     * @param email Email
     * @param password Mật khẩu (có thể null khi cập nhật)
     * @param phone Số điện thoại
     * @param userId ID người dùng (null khi thêm mới)
     * @throws IllegalArgumentException nếu có bất kỳ lỗi validate nào
     */
    public void validateUserInfo(String fullName, String email, String password, String phone, Integer userId) {
        if (fullName == null || fullName.trim().isEmpty()) {
            throw new IllegalArgumentException("Họ tên không được để trống");
        }
        
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email không được để trống");
        }
        
        if (!email.matches("^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$")) {
            throw new IllegalArgumentException("Email không hợp lệ");
        }
        
        if (isEmailExist(email, userId)) {
            throw new IllegalArgumentException("Email đã được sử dụng bởi người dùng khác");
        }
        
        if (phone != null && !phone.trim().isEmpty() && isPhoneExist(phone, userId)) {
            throw new IllegalArgumentException("Số điện thoại đã được sử dụng bởi người dùng khác");
        }
        
        // Chỉ kiểm tra mật khẩu khi thêm mới hoặc khi có cập nhật mật khẩu
        if (userId == null && (password == null || !isValidPassword(password))) {
            throw new IllegalArgumentException("Mật khẩu phải có ít nhất 8 ký tự và không chứa khoảng trắng");
        }
    }
    
    /**
     * Tăng số lượt thích cho người dùng
     * @param userId ID của người dùng (bác sĩ) cần tăng lượt thích
     * @return User đã được cập nhật
     * @throws IllegalArgumentException nếu không tìm thấy người dùng
     */
    public User increaseLikes(Integer userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng"));
        
        // Kiểm tra xem user này có phải là bác sĩ không
        if (!"doctor".equals(user.getRole().getRoleName())) {
            throw new IllegalArgumentException("Chỉ có thể tăng lượt thích cho bác sĩ");
        }
        
        if (user.getLikes() == null) {
            user.setLikes(1);
        } else {
            user.setLikes(user.getLikes() + 1);
        }
        
        return userRepository.save(user);
    }
    
    /**
     * Lấy danh sách bác sĩ được sắp xếp theo số lượt thích giảm dần
     * @return List<User> danh sách bác sĩ
     */
    public List<User> getDoctorsByLikes() {
        return userRepository.findByRoleRoleNameOrderByLikesDesc("doctor");
    }
}
