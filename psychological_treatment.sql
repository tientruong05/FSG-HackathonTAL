CREATE DATABASE psychological_treatment;
USE psychological_treatment;

CREATE TABLE roles (
    role_id INT AUTO_INCREMENT PRIMARY KEY,
    role_name VARCHAR(50) UNIQUE NOT NULL
);

CREATE TABLE users (
    user_id INT AUTO_INCREMENT PRIMARY KEY,
    role_id INT,
    full_name VARCHAR(100) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    phone VARCHAR(15),
    image VARCHAR(255) NOT NULL,              
    is_online BIT(1) DEFAULT 0,
    is_active BIT(1) DEFAULT 1,
    likes INT DEFAULT 0,
    FOREIGN KEY (role_id) REFERENCES roles(role_id)
);

CREATE TABLE chat_sessions (
    session_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT,
    doctor_id INT,
    start_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    end_time DATETIME,
    doctor_notes TEXT,
    FOREIGN KEY (user_id) REFERENCES users(user_id),
    FOREIGN KEY (doctor_id) REFERENCES users(user_id)
);

CREATE TABLE chat_messages (
    message_id INT AUTO_INCREMENT PRIMARY KEY,
    session_id INT NOT NULL,
    sender_id INT NOT NULL,
    message_content TEXT NOT NULL,
    sent_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    client_id VARCHAR(50),
    FOREIGN KEY (session_id) REFERENCES chat_sessions(session_id) ON DELETE CASCADE,
    FOREIGN KEY (sender_id) REFERENCES users(user_id)
);


CREATE TABLE articles (
    article_id INT AUTO_INCREMENT PRIMARY KEY,
    admin_id INT,
    title VARCHAR(255) NOT NULL,
    image VARCHAR(255) NOT NULL,
    content TEXT NOT NULL,
    views INT DEFAULT 0,             
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (admin_id) REFERENCES users(user_id)
);

CREATE TABLE chatbot_responses (
    response_id INT AUTO_INCREMENT PRIMARY KEY,
    question TEXT NOT NULL,
    answer TEXT NOT NULL
);

-- Thêm dữ liệu mẫu cho bảng roles
INSERT INTO roles (role_name) VALUES ('user'), ('doctor'), ('admin');

-- Thêm dữ liệu mẫu cho bảng users
INSERT INTO users (role_id, full_name, email, password, phone, image, is_online, is_active) VALUES
(1, 'Bệnh nhân A', 'benhnhana@gmail.com', '12345678', '0912345678', '/uploads/default.png', 1, 1),
(1, 'Bệnh nhân B', 'benhnhanb@gmail.com', '12345678', '0932145678', '/uploads/default.png', 1, 1),
(1, 'Bệnh nhân C', 'benhnhanc@gmail.com', '12345678', '0912345679', '/uploads/default.png', 1, 1),
(1, 'Phan Đức K', 'phanduck@gmail.com', '12345678', '0912345671', '/uploads/default.png', 1, 1),
(1, 'Phạm Văn Tài', 'phamtai@gmail.com', '12345678', '0912003344', '/uploads/default.png', 1, 1),
(1, 'Ngô Thị Mỹ', 'ngothimy@gmail.com', '12345678', '0933556677', '/uploads/default.png', 1, 1),
(1, 'Lương Văn Cường', 'cuongluong@gmail.com', '12345678', '0912998877', '/uploads/default.png', 1, 1),
(1, 'Bùi Thị Kim', 'buithikim@gmail.com', '12345678', '0933221144', '/uploads/default.png', 1, 1),
(1, 'Tạ Văn Hùng', 'tavanhung@gmail.com', '12345678', '0912334455', '/uploads/default.png', 1, 1),
(1, 'Trương Văn C', 'truongvanc@gmail.com', '12345678', '0932456789', '/uploads/default.png', 1, 1),
(2, 'Trần Thị Hoa', 'drhoa@gmail.com', '12345678', '0987654321', '/uploads/drhoa.png', 1, 1),  
(2, 'Nguyễn Văn Khoa', 'drkhoa@gmail.com', '12345678', '0988001100', '/uploads/drkhoa.png', 1, 1), 
(2, 'Trần Thị Như', 'drnhu@gmail.com', '12345678', '0977332211', '/uploads/drnhu.png', 1, 1), 
(2, 'Nguyễn Thị Nhi', 'drnhi@gmail.com', '12345678', '0988667744', '/uploads/drnhi.png', 1, 1), 
(2, 'Hoàng Thị Mai', 'drmai@gmail.com', '12345678', '0978234567', '/uploads/drmai.png', 1, 1), 
(2, 'Vũ Thị Lan', 'drlanvu@gmail.com', '12345678', '0977998855', '/uploads/drlan.png', 1, 1), 
(2, 'Đinh Công Hậu', 'drhau@gmail.com', '12345678', '0988445566', '/uploads/drhau.png', 1, 1), 
(2, 'Hoàng Văn Nam', 'drnam@gmail.com', '12345678', '0978123456', '/uploads/drnam.png', 1, 1), 
(2, 'Lê Thanh Hải', 'drhai@gmail.com', '12345678', '0987654322', '/uploads/drhai.png', 1, 1),
(3, 'Admin', 'admin@gmail.com', '12345678', '0909123456', '/uploads/default.png', 1, 1);


-- Thêm dữ liệu mẫu cho bảng articles
INSERT INTO articles (admin_id, title, image, content, views, created_at) VALUES
(20, 'Cách Giảm Stress Hiệu Quả', '/uploads/article-1.png', '<b><i><font size="5">Cuộc sống hiện đại với nhịp sống nhanh, áp lực công việc, học tập và các mối quan hệ xã hội đôi khi khiến chúng ta cảm thấy căng thẳng và mệt mỏi. Stress không chỉ ảnh hưởng đến tinh thần mà còn tác động xấu đến sức khỏe thể chất nếu không được kiểm soát đúng cách. Vậy làm thế nào để giảm stress hiệu quả? Dưới đây là một số phương pháp đơn giản nhưng rất hiệu quả mà bạn có thể áp dụng hằng ngày.</font></i></b><div><br></div><div><h3 data-start="549" data-end="585" class=""><strong data-start="553" data-end="585"><font size="4">1. Hít Thở Sâu và Thiền Định</font></strong></h3>
<p data-start="587" data-end="806" class="">Một trong những cách đơn giản nhất để làm dịu tâm trí là <strong data-start="644" data-end="659">hít thở sâu</strong>. Khi bạn căng thẳng, nhịp thở thường gấp gáp và nông. Hít sâu, giữ hơi trong vài giây và thở ra từ từ sẽ giúp giảm nhịp tim, làm dịu hệ thần kinh.</p>
<p data-start="808" data-end="973" class=""><strong data-start="808" data-end="822">Thiền định</strong> cũng là một phương pháp cực kỳ hiệu quả để giảm stress. Chỉ cần 10–15 phút mỗi ngày để tĩnh tâm, bạn sẽ cảm thấy tinh thần thoải mái và tập trung hơn.</p><h3 data-start="980" data-end="1015" class=""><strong data-start="984" data-end="1015"><font size="4">2. Tập Thể Dục Thường Xuyên</font></strong></h3><p data-start="808" data-end="973" class="">
</p><p data-start="1017" data-end="1240" class="">Vận động giúp cơ thể tiết ra <strong data-start="1046" data-end="1059">endorphin</strong> – một loại hormone giúp cải thiện tâm trạng. Bạn không cần phải tập luyện nặng, chỉ cần đi bộ, chạy bộ nhẹ, tập yoga hoặc đạp xe 30 phút mỗi ngày cũng đã đủ để cải thiện tinh thần.</p><h3 data-start="1247" data-end="1269" class=""><strong data-start="1251" data-end="1269"><font size="4">3. Ngủ Đủ Giấc</font></strong></h3><p data-start="1017" data-end="1240" class="">
</p><p data-start="1271" data-end="1471" class="">Giấc ngủ đóng vai trò rất quan trọng trong việc tái tạo năng lượng và giảm stress. Người trưởng thành nên ngủ từ <strong data-start="1384" data-end="1405">7–8 tiếng mỗi đêm</strong>. Thiếu ngủ có thể khiến bạn dễ cáu gắt, mệt mỏi và mất tập trung.</p><h3 data-start="1478" data-end="1506" class=""><strong data-start="1482" data-end="1506"><font size="4">4. Ăn Uống Lành Mạnh</font></strong></h3><p data-start="1271" data-end="1471" class="">
</p><p data-start="1508" data-end="1757" class="">Chế độ ăn uống cân bằng, giàu vitamin và khoáng chất giúp cơ thể khỏe mạnh và tăng sức đề kháng với căng thẳng. Hạn chế sử dụng các chất kích thích như <strong data-start="1660" data-end="1672">caffeine</strong>, <strong data-start="1674" data-end="1694">đường tinh luyện</strong>, và <strong data-start="1699" data-end="1711">rượu bia</strong> vì chúng có thể làm bạn cảm thấy lo lắng hơn.</p><h3 data-start="1764" data-end="1792" class=""><strong data-start="1768" data-end="1792"><font size="4">5. Tâm Sự và Chia Sẻ</font></strong></h3><p data-start="1508" data-end="1757" class="">
</p><p data-start="1794" data-end="2020" class="">Đôi khi, chỉ cần <strong data-start="1811" data-end="1848">trò chuyện với một người bạn thân</strong>, người thân trong gia đình hay một chuyên gia tâm lý cũng đã giúp bạn cảm thấy nhẹ nhõm hơn rất nhiều. Đừng ngại chia sẻ cảm xúc – đó là cách để giải tỏa stress lành mạnh.</p></div>', 500, CURRENT_TIMESTAMP),
(20, 'Tầm Quan Trọng Của Sức Khỏe Tâm Lý', '/uploads/article-2.png', '<b><i><font size="5">Khi nhắc đến “sức khỏe”, nhiều người thường chỉ nghĩ đến thể chất – tức là cơ thể có khỏe mạnh hay không. Tuy nhiên, <span data-start="285" data-end="304">sức khỏe tâm lý</span> cũng đóng vai trò quan trọng không kém và thậm chí còn là yếu tố nền tảng quyết định chất lượng cuộc sống. Vậy sức khỏe tâm lý là gì, và tại sao chúng ta cần chăm sóc nó mỗi ngày?</font></i></b><div><br></div><div><h3 data-start="491" data-end="524" class=""><strong data-start="495" data-end="524"><font size="4">1. Sức Khỏe Tâm Lý Là Gì?</font></strong></h3>
<p data-start="526" data-end="793" class="">Sức khỏe tâm lý là trạng thái mà một người có thể nhận thức rõ cảm xúc, kiểm soát hành vi, và xây dựng các mối quan hệ lành mạnh với người khác. Nó bao gồm khả năng đối mặt với áp lực, vượt qua khó khăn, duy trì tinh thần tích cực và cảm thấy ý nghĩa trong cuộc sống.</p><h3 data-start="800" data-end="854" class=""><strong data-start="804" data-end="854"><font size="4">2. Ảnh Hưởng Của Sức Khỏe Tâm Lý Đến Cuộc Sống</font></strong></h3><h4 data-start="856" data-end="901" class=""><font size="3">🧠 <strong data-start="864" data-end="901">Tác động đến tư duy và quyết định</strong></font></h4><p data-start="903" data-end="1049" class="">Người có sức khỏe tâm lý tốt sẽ đưa ra những quyết định sáng suốt hơn, quản lý cảm xúc tốt và giữ được sự bình tĩnh trong các tình huống khó khăn.</p><h4 data-start="1051" data-end="1095" class=""><font size="3">💬 <strong data-start="1059" data-end="1095" style="">Cải thiện các mối quan hệ xã hội</strong></font></h4><p data-start="1097" data-end="1222" class="">Tâm lý ổn định giúp con người dễ dàng kết nối, thấu hiểu và duy trì mối quan hệ tích cực với gia đình, bạn bè và đồng nghiệp.</p><h4 data-start="1224" data-end="1268" class=""><font size="3">💼 <strong data-start="1232" data-end="1268" style="">Ảnh hưởng đến năng suất làm việc</strong></font></h4><p data-start="1270" data-end="1452" class="">Người có tinh thần tốt sẽ làm việc hiệu quả hơn, sáng tạo hơn và có khả năng hợp tác nhóm cao hơn. Ngược lại, nếu bị căng thẳng kéo dài, hiệu suất công việc sẽ suy giảm nghiêm trọng.</p><h4 data-start="1454" data-end="1496" class=""><font size="3">❤️ <strong data-start="1462" data-end="1496" style="">Tác động đến sức khỏe thể chất</strong></font></h4><p data-start="526" data-end="793" class="">
</p><p data-start="1498" data-end="1690" class="">Stress, lo âu và trầm cảm kéo dài có thể dẫn đến các bệnh lý như cao huyết áp, tim mạch, rối loạn tiêu hóa và mất ngủ. Tâm lý không khỏe mạnh là nguyên nhân tiềm ẩn của nhiều bệnh lý mãn tính.</p><h3 data-start="1697" data-end="1754" class=""><strong data-start="1701" data-end="1754"><font size="4">3. Tại Sao Chúng Ta Cần Chăm Sóc Sức Khỏe Tâm Lý?</font></strong></h3><p data-start="1498" data-end="1690" class="">
</p><p data-start="1756" data-end="1989" class="">Cũng giống như việc bạn cần tập thể dục để giữ cho cơ thể khỏe mạnh, thì <strong data-start="1829" data-end="1877">việc chăm sóc tâm hồn cũng là điều thiết yếu</strong>. Khi tâm lý ổn định, bạn sẽ sống tích cực hơn, suy nghĩ lạc quan hơn và có khả năng vượt qua thử thách tốt hơn.</p><h3 data-start="1996" data-end="2040" class=""><strong data-start="2000" data-end="2040"><font size="4">4. Làm Gì Để Bảo Vệ Sức Khỏe Tâm Lý?</font></strong></h3><h3 data-start="1996" data-end="2040" class=""><ul><li><strong data-start="2044" data-end="2068" style="font-size: 16px;">Tự lắng nghe cảm xúc</strong><span style="font-size: 16px;">: Đừng cố chối bỏ hay ép buộc bản thân phải luôn mạnh mẽ. Hãy chấp nhận cảm xúc của mình và học cách giải tỏa.</span></li><li><strong data-start="2181" data-end="2199" style="font-size: 16px;">Tìm sự giúp đỡ</strong><span style="font-size: 16px;">: Khi cảm thấy quá tải, hãy tâm sự với người thân, bạn bè hoặc tìm đến chuyên gia tâm lý.</span></li><li><strong data-start="2291" data-end="2322" style="font-size: 16px;">Dành thời gian cho bản thân</strong><span style="font-size: 16px;">: Làm những việc mình yêu thích, nghỉ ngơi khi cần thiết và không ngừng nuôi dưỡng niềm vui trong cuộc sống.</span></li><li><strong data-start="2433" data-end="2465" style="font-size: 16px;">Thiết lập lối sống lành mạnh</strong><span style="font-size: 16px;">: Ngủ đủ giấc, ăn uống khoa học, vận động thường xuyên và tránh xa chất kích thích.</span></li></ul></h3><div><h2 data-start="2555" data-end="2570" class=""><strong data-start="2558" data-end="2570">Kết Luận</strong></h2>
<p data-start="2572" data-end="2826" class=""><font size="4"><i>Sức khỏe tâm lý là nền tảng cho mọi khía cạnh của cuộc sống: từ học tập, công việc đến các mối quan hệ cá nhân. Vì thế, <strong data-start="2692" data-end="2741">đừng chỉ chăm sóc thân thể mà quên đi tâm hồn</strong>. Một tinh thần khỏe mạnh sẽ dẫn lối bạn đến với cuộc sống hạnh phúc và trọn vẹn hơn.</i></font></p></div></div>', 1000, CURRENT_TIMESTAMP),
(20, 'Trị Liệu Tâm Lý Là Gì? Hiểu Đúng Để Tìm Đúng Hướng Điều Trị', '/uploads/article-3.png',
'<div>
  <b><i><font size="5">Trong cuộc sống hiện đại, áp lực tinh thần ngày càng tăng khiến nhiều người cần đến sự hỗ trợ từ trị liệu tâm lý. Nhưng không phải ai cũng hiểu đúng về phương pháp này.</font></i></b>
  <div><br></div>

  <h3><strong><font size="4">1. Trị Liệu Tâm Lý Là Gì?</font></strong></h3>
  <p>Trị liệu tâm lý (psychotherapy) là quá trình tương tác giữa thân chủ và chuyên gia trị liệu để giúp người gặp vấn đề tâm lý hiểu rõ cảm xúc, hành vi và tìm ra hướng đi phù hợp để cải thiện chất lượng sống.</p>

  <h3><strong><font size="4">2. Khi Nào Cần Đến Trị Liệu Tâm Lý?</font></strong></h3>
  <p>Bạn nên tìm đến trị liệu nếu gặp những dấu hiệu như: lo âu kéo dài, trầm cảm, mất ngủ, mất động lực sống, sang chấn tâm lý, hoặc gặp khó khăn trong các mối quan hệ.</p>

  <h3><strong><font size="4">3. Các Phương Pháp Trị Liệu Phổ Biến</font></strong></h3>
  <ul>
    <li><b>Liệu pháp nhận thức - hành vi (CBT):</b> giúp thay đổi suy nghĩ tiêu cực thành tích cực.</li>
    <li><b>Liệu pháp tâm động học:</b> khai thác nguyên nhân sâu xa từ quá khứ.</li>
    <li><b>Trị liệu theo hướng nhân văn:</b> tập trung vào tiềm năng và sự phát triển cá nhân.</li>
  </ul>

  <h3><strong><font size="4">4. Trị Liệu Có Hiệu Quả Không?</font></strong></h3>
  <p>Hiệu quả của trị liệu phụ thuộc vào sự phù hợp giữa thân chủ và chuyên gia, mức độ vấn đề và sự kiên trì của người tham gia. Nhiều nghiên cứu cho thấy trị liệu giúp cải thiện tâm lý rõ rệt sau 8–12 buổi.</p>

  <h3><strong><font size="4">5. Cần Chuẩn Bị Gì Trước Khi Trị Liệu?</font></strong></h3>
  <p>Bạn nên xác định mục tiêu cá nhân, cởi mở chia sẻ và kiên nhẫn với quá trình. Tìm đúng chuyên gia phù hợp là bước quan trọng đầu tiên.</p>

  <div><h2><strong>Kết Luận</strong></h2>
  <p><i>Trị liệu tâm lý không phải chỉ dành cho người bị "bệnh tâm thần". Đó là phương pháp khoa học, nhân văn, giúp bạn sống cân bằng hơn giữa những biến động cảm xúc của cuộc sống hiện đại.</i></p>
  </div>
</div>',
820, CURRENT_TIMESTAMP),
(20, 'Làm Việc Với Chuyên Gia Tâm Lý: Quy Trình Và Kỳ Vọng', '/uploads/article-4.png',
'<div>
  <b><i><font size="5">Nhiều người bối rối khi lần đầu đến gặp chuyên gia tâm lý: liệu họ sẽ hỏi gì? mình có phải nói hết mọi chuyện? và mình sẽ được giúp ra sao?</font></i></b>
  <div><br></div>

  <h3><strong><font size="4">1. Buổi Trị Liệu Đầu Tiên Diễn Ra Như Thế Nào?</font></strong></h3>
  <p>Buổi đầu tiên thường là buổi đánh giá. Chuyên gia sẽ hỏi bạn về lý do đến trị liệu, các vấn đề đang gặp phải, lịch sử sức khỏe tâm lý và kỳ vọng của bạn.</p>

  <h3><strong><font size="4">2. Bạn Không Bắt Buộc Phải Nói Tất Cả</font></strong></h3>
  <p>Hãy chia sẻ ở mức độ bạn cảm thấy an toàn. Trị liệu là quá trình, không phải cuộc phỏng vấn. Chuyên gia sẽ đồng hành cùng bạn một cách tôn trọng và không phán xét.</p>

  <h3><strong><font size="4">3. Bảo Mật Thông Tin Cá Nhân</font></strong></h3>
  <p>Mọi thông tin trong buổi trị liệu đều được bảo mật theo nguyên tắc nghề nghiệp. Chỉ trong trường hợp nguy cơ tự hại hoặc hại người khác, chuyên gia mới có thể báo cho người liên quan.</p>

  <h3><strong><font size="4">4. Bạn Có Thể Đặt Câu Hỏi</font></strong></h3>
  <p>Bạn hoàn toàn có thể hỏi về phương pháp trị liệu, thời gian, chi phí, và cách chuyên gia sẽ hỗ trợ bạn. Một chuyên gia tốt sẽ luôn sẵn sàng lắng nghe và giải đáp.</p>

  <div><h2><strong>Kết Luận</strong></h2>
  <p><i>Làm việc với chuyên gia tâm lý là hành trình khám phá và chữa lành. Sự tin tưởng, minh bạch và kiên trì sẽ giúp bạn đạt được những thay đổi tích cực bền vững.</i></p>
  </div>
</div>',
540, CURRENT_TIMESTAMP),
(20, 'Làm Sao Biết Mình Cần Gặp Chuyên Gia Tâm Lý?', '/uploads/article-5.png',
'<div>
  <b><i><font size="5">Bạn có từng cảm thấy mình “ổn” nhưng trong lòng lại đầy hỗn loạn? Có thể bạn đang cần sự hỗ trợ từ một chuyên gia tâm lý, ngay cả khi bạn không gặp vấn đề nghiêm trọng.</font></i></b>
  <div><br></div>

  <h3><strong><font size="4">1. Cảm Xúc Bất Ổn Kéo Dài</font></strong></h3>
  <p>Nếu bạn thường xuyên thấy buồn, lo âu, tức giận mà không rõ nguyên nhân, hoặc cảm thấy không thể kiểm soát cảm xúc, đó có thể là dấu hiệu bạn cần trợ giúp chuyên môn.</p>

  <h3><strong><font size="4">2. Mất Động Lực Sống</font></strong></h3>
  <p>Khi bạn không còn hứng thú với những điều mình từng yêu thích, cảm thấy mọi thứ vô nghĩa – đó có thể là biểu hiện sớm của trầm cảm hoặc kiệt sức tâm lý.</p>

  <h3><strong><font size="4">3. Khó Ngủ, Mất Ngủ Kéo Dài</font></strong></h3>
  <p>Rối loạn giấc ngủ là một trong những triệu chứng phổ biến của rối loạn tâm lý. Nếu bạn mất ngủ liên tục trong 2 tuần trở lên, hãy cân nhắc gặp chuyên gia.</p>

  <h3><strong><font size="4">4. Gặp Khó Khăn Trong Mối Quan Hệ</font></strong></h3>
  <p>Thường xuyên cãi vã, cảm thấy không thể giao tiếp hiệu quả với người thân hoặc đồng nghiệp, bạn có thể cần hỗ trợ để cải thiện kỹ năng giao tiếp và thiết lập ranh giới lành mạnh.</p>

  <div><h2><strong>Kết Luận</strong></h2>
  <p><i>Bạn không cần phải chờ đến khi mọi thứ sụp đổ mới tìm đến chuyên gia tâm lý. Đôi khi, chỉ cần một vài buổi trò chuyện chuyên sâu đã có thể giúp bạn nhìn rõ và sống lành mạnh hơn.</i></p>
  </div>
</div>',
470, CURRENT_TIMESTAMP),
(20, 'Âm Nhạc Trị Liệu – Khi Giai Điệu Chạm Tới Tâm Hồn', '/uploads/article-6.png',
'<div>
  <b><i><font size="5">Âm nhạc có thể làm dịu tâm trí, kích thích ký ức và giúp chữa lành cảm xúc. Nhưng bạn có biết rằng, âm nhạc còn được ứng dụng như một phương pháp trị liệu tâm lý hiệu quả?</font></i></b>
  <div><br></div>

  <h3><strong>1. Âm Nhạc Trị Liệu Là Gì?</strong></h3>
  <p>Âm nhạc trị liệu (Music Therapy) là phương pháp sử dụng âm nhạc có mục đích – như nghe, sáng tác, hát, hoặc di chuyển theo nhạc – để cải thiện sức khỏe tinh thần, cảm xúc và thể chất.</p>

  <h3><strong>2. Đối Tượng Nào Có Thể Hưởng Lợi?</strong></h3>
  <ul>
    <li>Người trầm cảm, lo âu hoặc stress kéo dài</li>
    <li>Trẻ em tự kỷ, gặp khó khăn trong giao tiếp</li>
    <li>Người cao tuổi mắc Alzheimer hoặc mất trí nhớ</li>
    <li>Người đang phục hồi sau sang chấn</li>
  </ul>

  <h3><strong>3. Âm Nhạc Ảnh Hưởng Đến Não Bộ Như Thế Nào?</strong></h3>
  <p>Âm nhạc kích thích vùng limbic (trung tâm cảm xúc) trong não, làm tăng dopamine và giảm cortisol – hai yếu tố quan trọng giúp cải thiện tâm trạng và giảm căng thẳng.</p>

  <h3><strong>4. Một Buổi Trị Liệu Âm Nhạc Diễn Ra Như Thế Nào?</strong></h3>
  <p>Tùy theo mục tiêu, thân chủ có thể:</p>
  <ul>
    <li>Nghe các bản nhạc được chọn lọc</li>
    <li>Thể hiện cảm xúc qua bài hát hoặc sáng tác</li>
    <li>Dùng nhạc cụ đơn giản (trống, chuông, kalimba...)</li>
    <li>Vận động hoặc thiền theo nhạc</li>
  </ul>

  <div><h2><strong>Kết Luận</strong></h2>
  <p><i>Âm nhạc trị liệu không chỉ là nghệ thuật, mà là một phương pháp khoa học giúp kết nối với cảm xúc sâu bên trong và hỗ trợ quá trình chữa lành tự nhiên của tâm trí.</i></p>
  </div>
</div>',
380, CURRENT_TIMESTAMP),
(20, 'Nghệ Thuật Trị Liệu – Khi Bức Vẽ Trở Thành Ngôn Ngữ Tâm Hồn', '/uploads/article-7.png',
'<div>
  <b><i><font size="5">Khi lời nói không thể diễn tả cảm xúc, nghệ thuật trở thành ngôn ngữ thay thế. Trị liệu bằng nghệ thuật là cách an toàn và sáng tạo để tiếp cận những vùng cảm xúc sâu thẳm.</font></i></b>
  <div><br></div>

  <h3><strong>1. Nghệ Thuật Trị Liệu Là Gì?</strong></h3>
  <p>Art Therapy là hình thức trị liệu tâm lý sử dụng hoạt động nghệ thuật như vẽ tranh, nặn đất sét, cắt dán... nhằm giúp cá nhân thể hiện, hiểu và điều tiết cảm xúc một cách sáng tạo.</p>

  <h3><strong>2. Tại Sao Nghệ Thuật Có Tác Dụng Trị Liệu?</strong></h3>
  <p>Hoạt động nghệ thuật kích hoạt não phải – nơi xử lý cảm xúc và hình ảnh – giúp tiếp cận các ký ức bị chôn giấu hoặc cảm xúc khó diễn tả bằng lời.</p>

  <h3><strong>3. Ai Có Thể Tham Gia?</strong></h3>
  <p>Art Therapy phù hợp với mọi độ tuổi, đặc biệt hiệu quả với:</p>
  <ul>
    <li>Trẻ gặp vấn đề hành vi hoặc học tập</li>
    <li>Người lớn có sang chấn tâm lý</li>
    <li>Người hướng nội, khó diễn đạt bằng lời</li>
  </ul>

  <h3><strong>4. Cần Biết Vẽ Mới Làm Được?</strong></h3>
  <p>Không! Trị liệu bằng nghệ thuật không đòi hỏi kỹ năng mỹ thuật. Mục tiêu không phải là "vẽ đẹp", mà là thể hiện cảm xúc chân thật nhất.</p>

  <div><h2><strong>Kết Luận</strong></h2>
  <p><i>Nghệ thuật giúp bạn giao tiếp với chính mình – một cách không phán xét, không ép buộc. Đó là bước đầu cho sự chữa lành thực sự từ bên trong.</i></p>
  </div>
</div>',
390, CURRENT_TIMESTAMP),
(20, 'Thiền Định Và Tâm Lý Trị Liệu – Sự Kết Hợp Mạnh Mẽ Cho Sức Khỏe Tinh Thần', '/uploads/article-8.png',
'<div>
  <b><i><font size="5">Bạn có biết? Chỉ 10 phút thiền mỗi ngày có thể giảm lo âu, tăng sự tập trung và cải thiện tâm trạng. Khi kết hợp với trị liệu tâm lý, thiền định trở thành công cụ hỗ trợ mạnh mẽ.</font></i></b>
  <div><br></div>

  <h3><strong>1. Thiền Định Là Gì?</strong></h3>
  <p>Thiền là trạng thái tập trung tâm trí vào hiện tại, thả lỏng suy nghĩ và chấp nhận cảm xúc một cách không phán xét. Có nhiều phương pháp như: thiền chánh niệm (mindfulness), thiền hơi thở, thiền hình dung...</p>

  <h3><strong>2. Tác Dụng Đã Được Khoa Học Công Nhận</strong></h3>
  <ul>
    <li>Giảm mức độ lo âu và căng thẳng</li>
    <li>Tăng khả năng tự điều tiết cảm xúc</li>
    <li>Cải thiện giấc ngủ và hệ miễn dịch</li>
    <li>Thúc đẩy sự tự nhận thức</li>
  </ul>

  <h3><strong>3. Kết Hợp Trong Trị Liệu Tâm Lý</strong></h3>
  <p>Nhiều chuyên gia tâm lý lồng ghép thiền vào buổi trị liệu để giúp thân chủ kết nối tốt hơn với cảm xúc, giảm phản ứng tiêu cực và tăng khả năng phục hồi tâm lý.</p>

  <h3><strong>4. Có Cần Ngồi Bất Động Hàng Giờ Không?</strong></h3>
  <p>Không! Thiền có thể bắt đầu từ 5–10 phút mỗi ngày. Có thể thiền khi đi bộ, rửa chén, hoặc đơn giản là chú ý vào hơi thở.</p>

  <div><h2><strong>Kết Luận</strong></h2>
  <p><i>Thiền không phải là trốn tránh thực tại, mà là học cách sống sâu sắc hơn trong từng khoảnh khắc. Kết hợp thiền và trị liệu là lựa chọn tuyệt vời cho một tâm trí khỏe mạnh và tĩnh tại.</i></p>
  </div>
</div>',
420, CURRENT_TIMESTAMP),
(20, 'Làm Chủ Cảm Xúc Trong Cuộc Sống Hằng Ngày', '/uploads/article-9.png',
'<div>
  <b><i><font size="5">Cảm xúc là một phần không thể thiếu trong cuộc sống con người. Tuy nhiên, nếu không biết cách kiểm soát, cảm xúc tiêu cực có thể ảnh hưởng đến các mối quan hệ, công việc và cả sức khỏe tinh thần.</font></i></b>
  <div><br></div>
  <h3><strong><font size="4">1. Hiểu Rõ Cảm Xúc Của Mình</font></strong></h3>
  <p>Mỗi cảm xúc đều có lý do xuất hiện. Học cách nhận diện cảm xúc – như buồn, tức giận, lo lắng – là bước đầu tiên để quản lý chúng. Viết nhật ký cảm xúc mỗi ngày có thể giúp bạn hiểu rõ hơn về những gì đang xảy ra bên trong mình.</p>

  <h3><strong><font size="4">2. Hít Thở và Phản Ứng Chậm Lại</font></strong></h3>
  <p>Khi cảm xúc tiêu cực bùng phát, hãy dừng lại, hít thở sâu và đếm từ 1 đến 10. Điều này giúp bạn có thời gian để suy nghĩ trước khi phản ứng, thay vì để cảm xúc điều khiển hành động.</p>

  <h3><strong><font size="4">3. Đặt Giới Hạn và Bảo Vệ Bản Thân</font></strong></h3>
  <p>Không phải lúc nào bạn cũng cần chiều lòng người khác. Học cách nói “không” khi cần thiết sẽ giúp bạn tránh được stress và giữ tâm lý ổn định hơn.</p>

  <h3><strong><font size="4">4. Tìm Sự Hỗ Trợ Khi Cần</font></strong></h3>
  <p>Đừng ngại tâm sự với bạn bè, người thân, hoặc chuyên gia tâm lý nếu cảm xúc tiêu cực kéo dài. Chia sẻ là cách để giải tỏa và tìm kiếm góc nhìn mới.</p>

  <div><h2><strong>Kết Luận</strong></h2>
  <p><i>Làm chủ cảm xúc là kỹ năng quan trọng giúp bạn sống hạnh phúc và thành công hơn. Hãy bắt đầu từ những điều nhỏ nhất: hiểu bản thân, điều chỉnh phản ứng và biết tự chăm sóc tâm hồn mình.</i></p>
  </div>
</div>',
650, CURRENT_TIMESTAMP),
(20, 'Thói Quen Buổi Sáng Giúp Tinh Thần Lạc Quan Cả Ngày', '/uploads/article-10.png',
'<div>
  <b><i><font size="5">Buổi sáng là thời điểm quan trọng định hình năng lượng và tâm trạng cho cả ngày. Việc thiết lập những thói quen tích cực ngay khi thức dậy có thể giúp bạn cải thiện sức khỏe tinh thần một cách bền vững.</font></i></b>
  <div><br></div>

  <h3><strong><font size="4">1. Thức Dậy Sớm và Không Dùng Điện Thoại Ngay</font></strong></h3>
  <p>Thay vì cuộn mình trong chăn và lướt mạng xã hội, hãy dậy sớm hơn 30 phút, dành thời gian yên tĩnh để làm mới bản thân.</p>

  <h3><strong><font size="4">2. Uống Một Ly Nước Ấm</font></strong></h3>
  <p>Việc uống nước giúp cơ thể khởi động nhẹ nhàng, thúc đẩy quá trình trao đổi chất và tăng cường sự tỉnh táo.</p>

  <h3><strong><font size="4">3. Vận Động Nhẹ Nhàng</font></strong></h3>
  <p>Không cần tập quá nặng, chỉ cần vài động tác kéo giãn cơ, yoga, hoặc đi bộ quanh nhà là đủ để khơi dậy nguồn năng lượng tích cực.</p>

  <h3><strong><font size="4">4. Thực Hành Lòng Biết Ơn</font></strong></h3>
  <p>Hãy viết ra 3 điều bạn biết ơn mỗi sáng – có thể là một giấc ngủ ngon, người thân bên cạnh, hoặc một cơ hội trong ngày mới.</p>

  <h3><strong><font size="4">5. Ăn Sáng Đủ Chất</font></strong></h3>
  <p>Một bữa sáng lành mạnh với protein, chất xơ và trái cây sẽ giúp bạn duy trì tinh thần minh mẫn, không mệt mỏi vào buổi trưa.</p>

  <div><h2><strong>Kết Luận</strong></h2>
  <p><i>Thay đổi một vài thói quen nhỏ vào buổi sáng có thể tạo nên tác động lớn đến sức khỏe tinh thần của bạn. Hãy bắt đầu ngày mới một cách chủ động, bạn sẽ cảm thấy yêu đời và làm việc hiệu quả hơn.</i></p>
  </div>
</div>',
880, CURRENT_TIMESTAMP),
(20, 'Tự Chăm Sóc Tâm Lý Khi Làm Việc Từ Xa', '/uploads/article-11.png',
'<div>
  <b><i><font size="5">Làm việc từ xa mang lại sự linh hoạt nhưng cũng có thể khiến bạn cảm thấy cô lập, mất cân bằng và căng thẳng kéo dài. Việc tự chăm sóc tâm lý là yếu tố then chốt để duy trì sự bền vững trong công việc và cuộc sống.</font></i></b>
  <div><br></div>

  <h3><strong><font size="4">1. Thiết Lập Ranh Giới Rõ Ràng</font></strong></h3>
  <p>Hãy phân biệt thời gian làm việc và nghỉ ngơi rõ ràng. Đừng mang laptop lên giường hoặc làm việc suốt cả ngày mà không có giờ nghỉ cụ thể.</p>

  <h3><strong><font size="4">2. Tạo Góc Làm Việc Riêng Biệt</font></strong></h3>
  <p>Một không gian làm việc yên tĩnh, gọn gàng sẽ giúp bạn tập trung hơn và dễ tách biệt khỏi đời sống cá nhân.</p>

  <h3><strong><font size="4">3. Giữ Liên Lạc Với Người Khác</font></strong></h3>
  <p>Trò chuyện qua video call, nhắn tin với đồng nghiệp hoặc bạn bè giúp bạn duy trì kết nối xã hội và cảm thấy không cô đơn.</p>

  <h3><strong><font size="4">4. Đừng Bỏ Qua Các Hoạt Động Cá Nhân</font></strong></h3>
  <p>Sau giờ làm, hãy dành thời gian cho sở thích như đọc sách, chơi nhạc, tập thể dục – đó là liều thuốc tinh thần quý giá.</p>

  <div><h2><strong>Kết Luận</strong></h2>
  <p><i>Làm việc từ xa hiệu quả đòi hỏi bạn phải chủ động chăm sóc sức khỏe tâm lý. Khi bạn cân bằng được công việc và cảm xúc, hiệu suất sẽ tăng lên và chất lượng cuộc sống cũng được cải thiện.</i></p>
  </div>
</div>',
730, CURRENT_TIMESTAMP);


select * from users;
select * from chat_sessions;
select * from articles;

delete from users;
delete from chat_sessions;
delete from articles;
delete from chatbot_responses;
