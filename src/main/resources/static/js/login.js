document.addEventListener("DOMContentLoaded", function () {
  const loginError = /*[[${loginError}]]*/ null;
  const registerError = /*[[${registerError}]]*/ null;
  const registerSuccess = /*[[${registerSuccess}]]*/ null;

  // Kiểm tra role người dùng hiện tại và áp dụng theme sáng nếu là admin hoặc bác sĩ
  checkUserRoleForTheme();

  if (loginError || registerError || registerSuccess) {
    const modal = new bootstrap.Modal(document.getElementById("authModal"));
    modal.show();
    if (registerError || registerSuccess) {
      const registerTab = new bootstrap.Tab(
        document.querySelector("#register-tab")
      );
      registerTab.show();
    }
  }
  document
    .querySelector(".switch-to-register")
    .addEventListener("click", function (e) {
      e.preventDefault();
      const registerTab = new bootstrap.Tab(
        document.querySelector("#register-tab")
      );
      registerTab.show();
    });
  document
    .querySelector(".switch-to-login")
    .addEventListener("click", function (e) {
      e.preventDefault();
      const loginTab = new bootstrap.Tab(document.querySelector("#login-tab"));
      loginTab.show();
    });
});

/**
 * Kiểm tra vai trò của người dùng hiện tại và áp dụng theme sáng nếu là admin hoặc bác sĩ
 */
function checkUserRoleForTheme() {
  const userRoleElement = document.getElementById("user-role-data");

  if (userRoleElement) {
    const userRole = userRoleElement.getAttribute("data-user-role");
    console.log("Login.js checking user role:", userRole);

    // Nếu người dùng là quản trị viên hoặc bác sĩ
    if (userRole === "admin" || userRole === "doctor") {
      console.log(
        "Login.js: Admin/Doctor role detected, enforcing light theme"
      );
      // Áp dụng theme sáng
      document.body.classList.remove("dark-mode");
      document.documentElement.classList.remove("dark-mode");
      localStorage.setItem("theme", "light");
    }
  }
}
