# Dự Án Hỗ Trợ Sức Khỏe Tâm Lý - FSGHackathonTAL

## Tổng Quan

Dự án FSGHackathonTAL là một nền tảng trực tuyến hỗ trợ sức khỏe tâm lý, cung cấp các giải pháp kết nối người dùng với chuyên gia tâm lý, chatbot AI hỗ trợ 24/7, và tài nguyên học tập về sức khỏe tâm thần. Mục tiêu chính của dự án là:

- Tạo không gian an toàn để người dùng chia sẻ vấn đề tâm lý
- Cung cấp hỗ trợ tâm lý nhanh chóng thông qua chatbot AI
- Kết nối người dùng với bác sĩ tâm lý chuyên nghiệp qua chat trực tuyến
- Cung cấp nguồn tài liệu, bài viết về sức khỏe tâm lý

## Công Nghệ Sử Dụng

### Backend

- **Java 23**
- **Spring Boot 3.4.4**: Framework phát triển ứng dụng
- **Spring Security**: Xác thực và phân quyền
- **Spring Data JPA**: Tương tác với cơ sở dữ liệu
- **WebSocket (STOMP)**: Giao tiếp thời gian thực cho chức năng chat
- **RESTful API**: Giao tiếp giữa client và server
- **Spring Actuator**: Giám sát và quản lý ứng dụng
- **Jakarta EE**: Jakarta Servlet, Persistence, Validation, và JSON APIs
- **Lombok**: Giảm boilerplate code

### Frontend

- **Thymeleaf**: Template engine kết hợp với Spring Boot
- **Bootstrap 5**: Framework CSS cho giao diện responsive
- **JavaScript/jQuery**: Xử lý tương tác người dùng
- **SockJS & STOMP.js**: Kết nối WebSocket để chat thời gian thực

### Cơ Sở Dữ Liệu

- **MySQL**: Lưu trữ dữ liệu người dùng, tin nhắn, phiên chat, v.v.
- **Hibernate**: ORM framework

### Tích Hợp Bên Ngoài

- **Google Gemini API**: Xử lý và tạo phản hồi cho chatbot tâm lý

## Chi Tiết Kỹ Thuật

### Controllers

#### 1. AuthController (Xác thực và Đăng nhập)

- **File chính**: `src/main/java/com/FSGHackathonTAL/controller/AuthController.java`
- **Templates liên quan**:
  - `home.html`: Trang chủ hệ thống
  - Fragments:
    - `fragments/auth-modal.html`: Modal đăng nhập/đăng ký
    - `fragments/navbar.html`: Thanh điều hướng
    - `fragments/footer.html`: Chân trang
    - `fragments/back-to-top.html`: Nút quay lại đầu trang
    - `fragments/doctors-modal.html`: Modal chọn bác sĩ
    - `fragments/therapy-theme.html`: Theme ứng dụng
- **Services sử dụng**:

  - `UserService`: Xử lý đăng ký, đăng nhập người dùng
  - `ChatSessionService`: Kiểm tra phiên chat hiện có của người dùng
  - `ArticleService`: Lấy bài viết mới cho trang chủ
  - `DoctorAvailabilityService`: Kiểm tra bác sĩ trực tuyến

- **Luồng xử lý chi tiết**:
  1. Đăng nhập `/login`:
     - Kiểm tra thông tin đăng nhập từ form
     - Xác thực với `UserService`
     - Tạo session và chuyển hướng về trang chủ
  2. Đăng ký `/register`:
     - Kiểm tra và validate thông tin đăng ký
     - Mã hóa mật khẩu
     - Lưu thông tin người dùng mới vào database
  3. Trang chủ `/home`:
     - Kiểm tra người dùng đã đăng nhập
     - Lấy danh sách bác sĩ đang online từ `DoctorAvailabilityService`
     - Lấy bài viết mới từ `ArticleService`
     - Kiểm tra phiên chat hoạt động từ `ChatSessionService`
     - Render trang home với đầy đủ thông tin

#### 2. ChatbotController (Hệ thống Chatbot Tâm lý)

- **File chính**: `src/main/java/com/FSGHackathonTAL/controller/ChatbotController.java`
- **Templates liên quan**:
  - `chatbot.html`: Giao diện trò chuyện với chatbot
  - Fragments:
    - `fragments/navbar.html`: Thanh điều hướng
    - `fragments/footer.html`: Chân trang
    - `fragments/therapy-theme.html`: Theme ứng dụng
- **Services sử dụng**:
  - `ChatbotService`: Xử lý câu hỏi và tạo câu trả lời
- **DTOs liên quan**:

  - `ChatbotMessageDTO`: Chứa thông tin tin nhắn trao đổi với chatbot

- **Luồng xử lý chi tiết**:
  1. Hiển thị giao diện chatbot `/chatbot`:
     - Kiểm tra đăng nhập và phân quyền
     - Trả về template `chatbot.html` với welcome message
  2. API xử lý tin nhắn `/api/chatbot/message` (POST):
     - Nhận `ChatbotMessageDTO` từ client
     - Gọi `ChatbotService.isMentalHealthRelated()` để kiểm tra nội dung tin nhắn
     - Nếu liên quan tâm lý, gọi `ChatbotService.getResponseAndSaveIfNew()`
     - Trả về phản hồi cho client hiển thị

#### 3. ChatController (Chat giữa người dùng và bác sĩ)

- **File chính**: `src/main/java/com/FSGHackathonTAL/controller/ChatController.java`
- **Templates liên quan**:
  - `chat.html`: Giao diện chat với bác sĩ
  - Fragments:
    - `fragments/navbar.html`: Thanh điều hướng
    - `fragments/footer.html`: Chân trang
    - `fragments/therapy-theme.html`: Theme ứng dụng
- **Services sử dụng**:
  - `ChatSessionService`: Quản lý phiên chat
  - `UserService`: Lấy thông tin người dùng
- **WebSocket endpoints**:

  - `/chat.sendMessage`: Nhận tin nhắn từ client
  - `/topic/chat.{sessionId}`: Gửi tin nhắn đến client

- **Luồng xử lý chi tiết**:
  1. Tin nhắn WebSocket `@MessageMapping("/chat.sendMessage")`:
     - Nhận `MessageDTO` từ client
     - Lưu tin nhắn với `ChatSessionService.saveMessage()`
     - Gửi tin nhắn tới topic WebSocket `/topic/chat.{sessionId}`
  2. API lấy danh sách tin nhắn `/api/chat/{sessionId}/messages` (GET):
     - Kiểm tra phân quyền người dùng
     - Gọi `ChatSessionService.getMessagesBySessionId()`
     - Trả về danh sách tin nhắn theo trang
  3. API kết thúc phiên chat `/api/chat/{sessionId}/end` (POST):
     - Kiểm tra vai trò người dùng
     - Gọi `ChatSessionService.endChatSession()`
     - Thông báo cho các bên qua WebSocket

#### 4. DoctorChatController (Quản lý chat từ phía bác sĩ)

- **File chính**: `src/main/java/com/FSGHackathonTAL/controller/DoctorChatController.java`
- **Templates liên quan**:
  - `doctor-dashboard.html`: Bảng điều khiển của bác sĩ
  - `doctor-chat.html`: Giao diện chat từ phía bác sĩ
  - Fragments:
    - `fragments/navbar.html`: Thanh điều hướng
    - `fragments/footer.html`: Chân trang
- **Services sử dụng**:
  - `ChatSessionService`: Quản lý phiên chat
  - `UserService`: Lấy thông tin bác sĩ
  - `DoctorAvailabilityService`: Quản lý trạng thái online của bác sĩ
- **WebSocket endpoints**:

  - `/doctor.sendMessage`: Nhận tin nhắn từ bác sĩ
  - `/topic/doctor.notifications`: Thông báo về phiên chat mới

- **Luồng xử lý chi tiết**:
  1. Trang dashboard bác sĩ `/doctor/dashboard`:
     - Kiểm tra đăng nhập và vai trò bác sĩ
     - Lấy danh sách phiên chat (`ChatSessionService.getActiveChatSessionsByDoctorId()`)
     - Render template `doctor-dashboard.html`
  2. API cập nhật trạng thái online `/api/doctor/availability` (POST):
     - Nhận trạng thái mới (online/offline)
     - Cập nhật với `DoctorAvailabilityService.updateAvailability()`
     - Thông báo cho hệ thống qua WebSocket
  3. API lưu ghi chú về phiên chat `/api/doctor/chat/{sessionId}/notes` (POST):
     - Nhận nội dung ghi chú từ bác sĩ
     - Lưu với `ChatSessionService.saveSessionNotes()`

#### 5. ArticleController (Quản lý bài viết)

- **File chính**: `src/main/java/com/FSGHackathonTAL/controller/ArticleController.java`
- **Templates liên quan**:
  - `articles.html`: Danh sách bài viết
  - `article-detail.html`: Chi tiết bài viết
  - Fragments:
    - `fragments/navbar.html`: Thanh điều hướng
    - `fragments/footer.html`: Chân trang
    - `fragments/back-to-top.html`: Nút quay lại đầu trang
    - `fragments/auth-modal.html`: Modal đăng nhập/đăng ký
    - `fragments/doctors-modal.html`: Modal chọn bác sĩ
    - `fragments/therapy-theme.html`: Theme ứng dụng
- **Services sử dụng**:
  - `ArticleService`: Quản lý bài viết

#### 6. RelaxController (Chức năng thư giãn)

- **File chính**: `src/main/java/com/FSGHackathonTAL/controller/RelaxController.java`
- **Templates liên quan**:
  - `IDWTDA.html`: Trang thư giãn với hướng dẫn hít thở
- **Services sử dụng**:
  - Không có service đặc biệt, chỉ render template

### Services

#### 1. ChatbotService

- **File chính**: `src/main/java/com/FSGHackathonTAL/service/ChatbotService.java`
- **Repositories sử dụng**: `ChatbotResponseRepository`
- **Models liên quan**: `ChatbotResponse`

- **Chức năng chi tiết**:

  - `filterPersonalInfo(String question)`: Lọc thông tin cá nhân trong câu hỏi

    - Sử dụng regex để nhận diện và loại bỏ thông tin nhạy cảm
    - Hỗ trợ cả văn bản có dấu và không dấu
    - Thay thế thông tin cá nhân bằng placeholder

  - `isMentalHealthRelated(String question)`: Kiểm tra câu hỏi có liên quan tâm lý

    - Sử dụng danh sách từ khóa (`MENTAL_HEALTH_KEYWORDS`)
    - Tìm kiếm từ khóa trong câu hỏi (không phân biệt chữ hoa/thường)

  - `getResponseAndSaveIfNew(String question)`: Xử lý câu hỏi và lấy câu trả lời

    - Lọc thông tin cá nhân từ câu hỏi gốc
    - Tìm câu trả lời trong database
    - Nếu không có, gọi API Gemini
    - Lưu câu hỏi và câu trả lời mới vào database

  - `getResponseFromGemini(String question)`: Gọi Google Gemini API

    - Chuẩn bị HTTP request với câu hỏi đã lọc
    - Thêm system prompt để hướng dẫn AI
    - Xử lý response từ API

  - `getSimulatedAIResponse(String question)`: Tạo câu trả lời mẫu khi API không khả dụng
    - Phân tích từ khóa trong câu hỏi
    - Trả về mẫu câu trả lời phù hợp với chủ đề

#### 2. ChatSessionService

- **File chính**: `src/main/java/com/FSGHackathonTAL/service/ChatSessionService.java`
- **Repositories sử dụng**: `ChatSessionRepository`, `MessageRepository`
- **Models liên quan**: `ChatSession`, `Message`, `User`

- **Chức năng chi tiết**:

  - `createChatSession(User user, User doctor)`: Tạo phiên chat mới

    - Kiểm tra phiên chat hiện có
    - Tạo và lưu phiên chat mới giữa người dùng và bác sĩ
    - Đặt trạng thái phiên chat là "active"

  - `getActiveChatSessionsByDoctorId(Long doctorId)`: Lấy danh sách phiên chat đang hoạt động của bác sĩ

    - Lọc theo trạng thái phiên chat là "active"
    - Sắp xếp theo thời gian cập nhật mới nhất

  - `getActiveChatSessionByUserId(Long userId)`: Lấy phiên chat đang hoạt động của người dùng

    - Trả về phiên chat "active" hiện tại nếu có
    - Trả về null nếu không có phiên nào đang hoạt động

  - `endChatSession(Long sessionId)`: Kết thúc phiên chat

    - Cập nhật trạng thái phiên chat sang "ended"
    - Cập nhật thời gian kết thúc phiên

  - `saveMessage(MessageDTO messageDTO)`: Lưu tin nhắn mới

    - Chuyển đổi từ DTO sang entity
    - Liên kết tin nhắn với phiên chat tương ứng
    - Cập nhật thời gian cập nhật mới nhất của phiên chat

  - `getMessagesBySessionId(Long sessionId, int page, int size)`: Lấy danh sách tin nhắn theo trang

    - Kiểm tra quyền truy cập vào phiên chat
    - Phân trang tin nhắn
    - Sắp xếp theo thời gian gửi tăng dần

  - `saveSessionNotes(Long sessionId, String notes)`: Lưu ghi chú của bác sĩ về phiên chat
    - Xác thực quyền của bác sĩ đối với phiên chat
    - Cập nhật ghi chú vào phiên chat

#### 3. UserService

- **File chính**: `src/main/java/com/FSGHackathonTAL/service/UserService.java`
- **Repositories sử dụng**: `UserRepository`, `RoleRepository`
- **Models liên quan**: `User`, `Role`, `AuthRequest`, `AuthResponse`

- **Chức năng chi tiết**:

  - `registerUser(RegisterRequest request)`: Đăng ký người dùng mới

    - Kiểm tra trùng lặp email
    - Mã hóa mật khẩu
    - Gán vai trò "USER" cho người dùng mới
    - Lưu thông tin người dùng vào database

  - `loginUser(AuthRequest request)`: Xác thực đăng nhập

    - Tìm kiếm người dùng theo email
    - Kiểm tra mật khẩu
    - Tạo JWT token nếu xác thực thành công
    - Cập nhật trạng thái online cho người dùng

  - `getUserById(Long userId)`: Lấy thông tin người dùng theo ID

    - Kiểm tra người dùng tồn tại
    - Trả về thông tin người dùng

  - `getDoctorById(Long doctorId)`: Lấy thông tin bác sĩ theo ID

    - Kiểm tra người dùng tồn tại và có vai trò "DOCTOR"
    - Trả về thông tin bác sĩ

  - `getAllDoctors()`: Lấy danh sách tất cả bác sĩ

    - Lọc người dùng có vai trò "DOCTOR"
    - Trả về danh sách bác sĩ

  - `updateUserProfile(UserUpdateRequest request)`: Cập nhật thông tin cá nhân

    - Xác thực người dùng hiện tại
    - Cập nhật thông tin cá nhân (không bao gồm mật khẩu)
    - Lưu thông tin đã cập nhật

  - `changePassword(PasswordChangeRequest request)`: Đổi mật khẩu
    - Xác thực mật khẩu hiện tại
    - Mã hóa mật khẩu mới
    - Cập nhật mật khẩu

#### 4. DoctorAvailabilityService

- **File chính**: `src/main/java/com/FSGHackathonTAL/service/DoctorAvailabilityService.java`
- **Repositories sử dụng**: `UserRepository`, `DoctorAvailabilityRepository`
- **Models liên quan**: `User`, `DoctorAvailability`

- **Chức năng chi tiết**:

  - `updateAvailability(Long doctorId, boolean isAvailable)`: Cập nhật trạng thái online của bác sĩ

    - Kiểm tra người dùng có vai trò "DOCTOR"
    - Cập nhật trạng thái online/offline
    - Lưu thời gian cập nhật trạng thái

  - `getAvailableDoctors()`: Lấy danh sách bác sĩ đang online

    - Lọc bác sĩ có trạng thái "available"
    - Sắp xếp theo thời gian online

  - `getBusyDoctors()`: Lấy danh sách bác sĩ đang bận

    - Lọc bác sĩ có trạng thái "busy" (đang trong phiên chat)
    - Sắp xếp theo thời gian cập nhật

  - `setDoctorBusy(Long doctorId)`: Đặt trạng thái bác sĩ sang bận

    - Cập nhật trạng thái thành "busy"
    - Ghi lại thời gian bắt đầu bận

  - `setDoctorAvailable(Long doctorId)`: Đặt trạng thái bác sĩ sang sẵn sàng

    - Cập nhật trạng thái thành "available"
    - Ghi lại thời gian bắt đầu sẵn sàng

  - `getAutoUpdateStatus()`: Tự động cập nhật trạng thái
    - Kiểm tra bác sĩ offline quá lâu và cập nhật trạng thái
    - Chạy định kỳ bằng Spring Scheduler

#### 5. ArticleService

- **File chính**: `src/main/java/com/FSGHackathonTAL/service/ArticleService.java`
- **Repositories sử dụng**: `ArticleRepository`, `ArticleTagRepository`
- **Models liên quan**: `Article`, `ArticleTag`, `ArticleDTO`

- **Chức năng chi tiết**:

  - `getAllArticles(int page, int size)`: Lấy danh sách bài viết phân trang

    - Phân trang bài viết
    - Sắp xếp theo ngày đăng mới nhất
    - Chuyển đổi từ entity sang DTO

  - `getFeaturedArticles(int limit)`: Lấy danh sách bài viết nổi bật

    - Lọc bài viết có thuộc tính "featured"
    - Giới hạn số lượng trả về
    - Sắp xếp theo thứ tự ưu tiên

  - `getArticlesByTag(String tagName, int page, int size)`: Lấy bài viết theo thẻ

    - Tìm bài viết theo thẻ
    - Phân trang kết quả
    - Sắp xếp theo ngày đăng mới nhất

  - `getArticleById(Long id)`: Lấy chi tiết bài viết theo ID

    - Kiểm tra bài viết tồn tại
    - Tăng số lượt xem bài viết
    - Chuyển đổi từ entity sang DTO

  - `getRelatedArticles(Long articleId, int limit)`: Lấy bài viết liên quan

    - Tìm bài viết có cùng thẻ
    - Loại trừ bài viết hiện tại
    - Giới hạn số lượng trả về

  - `searchArticles(String keyword, int page, int size)`: Tìm kiếm bài viết
    - Tìm kiếm trong tiêu đề và nội dung
    - Phân trang kết quả
    - Sắp xếp theo độ phù hợp

#### 6. RoleService

- **File chính**: `src/main/java/com/FSGHackathonTAL/service/RoleService.java`
- **Repositories sử dụng**: `RoleRepository`
- **Models liên quan**: `Role`

- **Chức năng chi tiết**:

  - `getRoleByName(String roleName)`: Lấy thông tin vai trò theo tên
    - Tìm kiếm vai trò trong database
    - Trả về thông tin vai trò nếu tìm thấy
    - Ném ngoại lệ nếu không tìm thấy vai trò
