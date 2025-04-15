package com.FSGHackathonTAL.repository;

import com.FSGHackathonTAL.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface để quản lý các thực thể User.
 * Cung cấp các phương thức để tìm kiếm và truy vấn thông tin người dùng.
 */
public interface UserRepository extends JpaRepository<User, Integer> {
    /**
     * Tìm kiếm người dùng dựa trên địa chỉ email.
     * @param email Địa chỉ email cần tìm kiếm.
     * @return Optional chứa User nếu tìm thấy, ngược lại là Optional rỗng.
     */
    Optional<User> findByEmail(String email);

    /**
     * Tìm kiếm người dùng dựa trên địa chỉ email và mật khẩu.
     * (Lưu ý: Lưu trữ và so sánh mật khẩu trực tiếp không an toàn, nên sử dụng mã hóa).
     * @param email Địa chỉ email.
     * @param password Mật khẩu.
     * @return Optional chứa User nếu tìm thấy thông tin đăng nhập khớp, ngược lại là Optional rỗng.
     */
    Optional<User> findByEmailAndPassword(String email, String password);

    /**
     * Lấy danh sách các bác sĩ đang trực tuyến.
     * Sử dụng truy vấn JPQL để lọc các người dùng có vai trò 'doctor' và trạng thái 'isOnline' là true.
     * @return Danh sách các User là bác sĩ đang trực tuyến.
     */
    @Query("SELECT u FROM User u WHERE u.role.roleName = 'doctor' AND u.isOnline = true")
    List<User> findOnlineDoctors();

    /**
     * Lấy danh sách tất cả người dùng có một vai trò cụ thể.
     * @param roleName Tên vai trò cần tìm kiếm (ví dụ: 'doctor', 'user').
     * @return Danh sách các User có vai trò được chỉ định.
     */
    List<User> findByRoleRoleName(String roleName);

    /**
     * Lấy danh sách người dùng có một vai trò cụ thể, có phân trang.
     * @param roleName Tên vai trò cần tìm kiếm.
     * @param pageable Thông tin phân trang.
     * @return Trang (Page) chứa các User có vai trò được chỉ định.
     */
    Page<User> findByRoleRoleName(String roleName, Pageable pageable);

    /**
     * Lấy danh sách người dùng có một vai trò cụ thể, sắp xếp theo số lượt thích giảm dần.
     * @param roleName Tên vai trò cần tìm kiếm (thường là 'doctor').
     * @return Danh sách các User có vai trò được chỉ định, đã sắp xếp theo lượt thích.
     */
    List<User> findByRoleRoleNameOrderByLikesDesc(String roleName);

    /**
     * Lấy danh sách người dùng có một vai trò cụ thể, sắp xếp theo số lượt thích giảm dần, có phân trang.
     * @param roleName Tên vai trò cần tìm kiếm (thường là 'doctor').
     * @param pageable Thông tin phân trang.
     * @return Trang (Page) chứa các User có vai trò được chỉ định, đã sắp xếp theo lượt thích.
     */
    Page<User> findByRoleRoleNameOrderByLikesDesc(String roleName, Pageable pageable);
}
