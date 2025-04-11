/**
 * Theme Manager - Quản lý giao diện sáng/tối
 * Chức năng: Cho phép người dùng chuyển đổi giữa theme sáng và tối
 */

// Đảm bảo rằng script này chỉ chạy một lần
if (typeof window.themeManagerInitialized === "undefined") {
  window.themeManagerInitialized = true;

  document.addEventListener("DOMContentLoaded", function () {
    console.log("Theme manager initializing...");

    // Kiểm tra vai trò của người dùng trước khi khởi tạo theme
    checkUserRoleAndApplyTheme();

    // Đảm bảo nút bật/tắt luôn được gắn sự kiện, ngay cả khi DOM thay đổi
    setupThemeToggleButton();

    // Thêm bộ quan sát để đảm bảo nút bật/tắt luôn được gắn sự kiện
    // khi có thay đổi trong DOM (như khi thêm/xóa phần tử)
    const observer = new MutationObserver(function () {
      setupThemeToggleButton();
    });

    observer.observe(document.body, {
      childList: true,
      subtree: true,
    });
  });

  /**
   * Kiểm tra vai trò của người dùng và áp dụng theme phù hợp
   * Quản trị viên và Bác sĩ luôn sử dụng theme sáng
   */
  function checkUserRoleAndApplyTheme() {
    const userRoleElement = document.getElementById("user-role-data");

    if (userRoleElement) {
      const userRole = userRoleElement.getAttribute("data-user-role");
      console.log("Checking user role for theme:", userRole);

      // Nếu người dùng là quản trị viên hoặc bác sĩ, luôn áp dụng theme sáng
      if (userRole === "admin" || userRole === "doctor") {
        console.log("Admin or Doctor detected, applying light theme");
        applyLightTheme();
        localStorage.setItem("theme", "light"); // Lưu lại để khi làm mới trang vẫn giữ theme sáng
        return; // Không cần khởi tạo theme từ localStorage
      }
    }

    // Khởi tạo theme từ localStorage cho các vai trò khác
    initializeTheme();
  }

  /**
   * Thiết lập sự kiện cho nút bật/tắt theme
   */
  function setupThemeToggleButton() {
    const themeToggleBtn = document.getElementById("theme-toggle");

    if (
      themeToggleBtn &&
      !themeToggleBtn.hasAttribute("data-theme-initialized")
    ) {
      console.log("Setting up theme toggle button");

      // Đánh dấu nút đã được khởi tạo để tránh gắn nhiều lần
      themeToggleBtn.setAttribute("data-theme-initialized", "true");

      // Gắn sự kiện click để bật/tắt theme
      themeToggleBtn.addEventListener("click", function (e) {
        console.log("Theme toggle button clicked");
        e.preventDefault();
        toggleTheme();
      });

      // Cập nhật icon ban đầu dựa trên theme hiện tại
      updateThemeIcon();
    }
  }

  /**
   * Khởi tạo theme từ localStorage hoặc mặc định là sáng
   */
  function initializeTheme() {
    const savedTheme = localStorage.getItem("theme");
    console.log("Saved theme:", savedTheme);

    if (savedTheme === "dark") {
      // Nếu đã lưu theme tối, áp dụng nó
      applyDarkTheme();
    } else {
      // Mặc định hoặc đã lưu theme sáng, áp dụng theme sáng
      applyLightTheme();
    }
  }

  /**
   * Chuyển đổi giữa theme sáng và tối
   */
  function toggleTheme() {
    console.log(
      "Toggling theme, current body classes:",
      document.body.className
    );

    if (document.body.classList.contains("dark-mode")) {
      console.log("Switching to light theme");
      applyLightTheme();
      localStorage.setItem("theme", "light");
    } else {
      console.log("Switching to dark theme");
      applyDarkTheme();
      localStorage.setItem("theme", "dark");
    }

    // Cập nhật icon sau khi bật/tắt
    updateThemeIcon();
  }

  /**
   * Áp dụng theme tối
   */
  function applyDarkTheme() {
    document.body.classList.add("dark-mode");
    document.documentElement.classList.add("dark-mode");
    console.log("Dark theme applied, body classes:", document.body.className);
  }

  /**
   * Áp dụng theme sáng
   */
  function applyLightTheme() {
    document.body.classList.remove("dark-mode");
    document.documentElement.classList.remove("dark-mode");
    console.log("Light theme applied, body classes:", document.body.className);
  }

  /**
   * Cập nhật icon dựa trên theme hiện tại
   */
  function updateThemeIcon() {
    const themeToggleBtn = document.getElementById("theme-toggle");

    if (themeToggleBtn) {
      const themeIcon = themeToggleBtn.querySelector("i");

      if (document.body.classList.contains("dark-mode")) {
        // Nếu đang ở theme tối, hiển thị icon mặt trời để chuyển sang theme sáng
        if (themeIcon) {
          themeIcon.className = "bi bi-sun-fill";
          themeToggleBtn.setAttribute("title", "Chuyển sang chế độ sáng");
          console.log("Updated icon to sun for dark mode");
        }
      } else {
        // Nếu đang ở theme sáng, hiển thị icon mặt trăng để chuyển sang theme tối
        if (themeIcon) {
          themeIcon.className = "bi bi-moon-fill";
          themeToggleBtn.setAttribute("title", "Chuyển sang chế độ tối");
          console.log("Updated icon to moon for light mode");
        }
      }
    }
  }
}
