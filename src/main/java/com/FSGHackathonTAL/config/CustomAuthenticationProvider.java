package com.FSGHackathonTAL.config;

import com.FSGHackathonTAL.entity.User;
import com.FSGHackathonTAL.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.Collections;

/**
 * Cung cấp logic xác thực tùy chỉnh cho ứng dụng.
 * Triển khai giao diện AuthenticationProvider của Spring Security để xác thực người dùng
 * dựa trên email và mật khẩu thông qua UserService.
 */
@Component
public class CustomAuthenticationProvider implements AuthenticationProvider {

    @Autowired
    private UserService userService;

    /**
     * Xác thực người dùng dựa trên thông tin đăng nhập được cung cấp.
     * Kiểm tra email, mật khẩu, trạng thái tài khoản (isActive) và gán quyền (role).
     *
     * @param authentication Đối tượng Authentication chứa thông tin đăng nhập (email, password).
     * @return UsernamePasswordAuthenticationToken nếu xác thực thành công, chứa thông tin User và quyền.
     * @throws AuthenticationException Nếu thông tin đăng nhập không hợp lệ hoặc tài khoản bị vô hiệu hóa.
     */
    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String email = authentication.getName();
        String password = authentication.getCredentials().toString();
        
        try {
            User user = userService.login(email, password);
            
            // Đảm bảo tài khoản người dùng đang hoạt động
            if (!user.getIsActive()) {
                throw new BadCredentialsException("Tài khoản đã bị vô hiệu hóa");
            }
            
            // Tạo quyền hạn dựa trên vai trò của người dùng
            SimpleGrantedAuthority authority = new SimpleGrantedAuthority(user.getRole().getRoleName());
            
            // Trả về token xác thực thành công với thông tin User và quyền hạn
            return new UsernamePasswordAuthenticationToken(
                user, 
                password, // Thông thường không nên trả về mật khẩu ở đây
                Collections.singleton(authority)
            );
        } catch (IllegalArgumentException e) {
            // Bắt ngoại lệ từ userService.login nếu thông tin không hợp lệ
            throw new BadCredentialsException(e.getMessage());
        }
    }

    /**
     * Kiểm tra xem Provider này có hỗ trợ loại Authentication được cung cấp hay không.
     *
     * @param authentication Loại lớp Authentication cần kiểm tra.
     * @return true nếu Provider hỗ trợ UsernamePasswordAuthenticationToken, ngược lại là false.
     */
    @Override
    public boolean supports(Class<?> authentication) {
        // Chỉ hỗ trợ loại xác thực UsernamePasswordAuthenticationToken
        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
