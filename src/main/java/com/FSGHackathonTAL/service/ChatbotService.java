package com.FSGHackathonTAL.service;

import com.FSGHackathonTAL.entity.ChatbotResponse;
import com.FSGHackathonTAL.repository.ChatbotResponseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class ChatbotService {

    @Autowired
    private ChatbotResponseRepository chatbotResponseRepository;
    
    @Autowired
    private RestTemplate restTemplate;
    
    @Value("${gemini.api.key}")
    private String geminiApiKey;
    
    @Value("${gemini.api.endpoint:https://generativelanguage.googleapis.com/v1beta/models/gemini-pro:generateContent}")
    private String geminiApiEndpoint;
    
    // Danh sách các pattern regex để nhận dạng thông tin cá nhân
    private static final List<Pattern> PERSONAL_INFO_PATTERNS = Arrays.asList(
        // Họ tên dạng "Nguyễn Văn A" hoặc "Nguyen Van A"
        Pattern.compile("(?i)(tên (tôi|mình|của tôi|em|chị|anh|cháu) là |tôi là |mình là |tên |họ tên |[Tt]ôi tên là )([A-Za-zÀ-ỹ\\s]{2,30})"),
        // Phiên bản không dấu
        Pattern.compile("(?i)(ten (toi|minh|cua toi|em|chi|anh|chau) la |toi la |minh la |ten |ho ten |[Tt]oi ten la )([A-Za-z\\s]{2,30})"),
        
        // Số điện thoại Việt Nam
        Pattern.compile("\\b(0[1-9][0-9]{8}|\\+84[1-9][0-9]{8})\\b"),
        
        // Email
        Pattern.compile("\\b[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}\\b"),
        
        // Số nhà, đường phố
        Pattern.compile("(?i)(số [0-9]{1,4}[A-Za-z]?|[0-9]{1,4}[A-Za-z]?/?[0-9]{0,4}[A-Za-z]?) (?:đường|phố|ngõ|ngách|hẻm|tổ|khu phố|khu đô thị|khu) [A-Za-zÀ-ỹ\\s0-9\\,\\-\\/]{2,50}"),
        // Phiên bản không dấu
        Pattern.compile("(?i)(so [0-9]{1,4}[A-Za-z]?|[0-9]{1,4}[A-Za-z]?/?[0-9]{0,4}[A-Za-z]?) (?:duong|pho|ngo|ngach|hem|to|khu pho|khu do thi|khu) [A-Za-z\\s0-9\\,\\-\\/]{2,50}"),
        
        // Khu vực, phường/xã, quận/huyện, tỉnh/thành phố
        Pattern.compile("(?i)(phường|xã|quận|huyện|thị xã|thành phố|tỉnh) [A-Za-zÀ-ỹ\\s0-9\\,\\-\\/]{2,30}"),
        // Phiên bản không dấu
        Pattern.compile("(?i)(phuong|xa|quan|huyen|thi xa|thanh pho|tinh) [A-Za-z\\s0-9\\,\\-\\/]{2,30}"),
        
        // Năm sinh, ngày sinh
        Pattern.compile("(?i)(sinh ngày|ngày sinh|sinh năm|năm sinh|dob) [0-9]{1,2}[\\-\\/\\.][0-9]{1,2}[\\-\\/\\.][0-9]{2,4}"),
        // Phiên bản không dấu
        Pattern.compile("(?i)(sinh ngay|ngay sinh|sinh nam|nam sinh|dob) [0-9]{1,2}[\\-\\/\\.][0-9]{1,2}[\\-\\/\\.][0-9]{2,4}"),
        
        // Sinh năm
        Pattern.compile("(?i)(sinh năm|năm sinh) [0-9]{4}"),
        // Phiên bản không dấu
        Pattern.compile("(?i)(sinh nam|nam sinh) [0-9]{4}"),
        
        // CMND, CCCD, passport
        Pattern.compile("(?i)((số |mã |)(CMND|CCCD|căn cước|chứng minh nhân dân|hộ chiếu|passport) ?(là |của tôi là |)([0-9]{9}|[0-9]{12}))"),
        // Phiên bản không dấu
        Pattern.compile("(?i)((so |ma |)(CMND|CCCD|can cuoc|chung minh nhan dan|ho chieu|passport) ?(la |cua toi la |)([0-9]{9}|[0-9]{12}))"),
        
        // Địa chỉ full
        Pattern.compile("(?i)(địa chỉ|nhà|nơi ở|chỗ ở|residence) (là |của tôi là |)(ở |tại |)[A-Za-zÀ-ỹ\\s0-9\\,\\-\\/]{5,100}"),
        // Phiên bản không dấu
        Pattern.compile("(?i)(dia chi|nha|noi o|cho o|residence) (la |cua toi la |)(o |tai |)[A-Za-z\\s0-9\\,\\-\\/]{5,100}")
    );
    
    // Danh sách các từ khóa chỉ địa điểm riêng để lọc
    private static final List<String> LOCATION_KEYWORDS = Arrays.asList(
        // Phiên bản có dấu
        "Hà Nội", "TPHCM", "TP HCM", "Thành phố Hồ Chí Minh", "Sài Gòn", "Đà Nẵng", "Hải Phòng", "Cần Thơ",
        "Biên Hòa", "Nha Trang", "Vinh", "Huế", "Quảng Ninh", "Hạ Long", "Hải Dương", "Thái Nguyên",
        "Vũng Tàu", "Bắc Ninh", "Thanh Hóa", "Bắc Giang", "Nam Định", "Thái Bình", 
        
        // Phiên bản không dấu
        "Ha Noi", "Thanh pho Ho Chi Minh", "Sai Gon", "Da Nang", "Hai Phong", "Can Tho",
        "Bien Hoa", "Vinh", "Hue", "Quang Ninh", "Ha Long", "Hai Duong", "Thai Nguyen",
        "Vung Tau", "Bac Ninh", "Thanh Hoa", "Bac Giang", "Nam Dinh", "Thai Binh"
    );
    
    // List of keywords related to mental health to filter questions
    private static final List<String> MENTAL_HEALTH_KEYWORDS = Arrays.asList(
        "lo âu", "trầm cảm", "stress", "căng thẳng", "buồn", "lo lắng", "sợ hãi",
        "hoảng loạn", "cô đơn", "tâm lý", "tinh thần", "sức khỏe tâm thần", "tự tử",
        "tổn thương", "đau khổ", "mất ngủ", "cảm xúc", "khó chịu", "tức giận", 
        "giận dữ", "mất tập trung", "rối loạn", "mệt mỏi", "kiệt sức", "áp lực",
        "sang chấn", "trauma", "ám ảnh", "suy nghĩ tiêu cực", "depression", "anxiety",
        "mental health", "psychological", "therapy", "therapist", "counseling",
        "đau đầu", "nhức đầu", "đau bụng", "chóng mặt", "mất động lực", "thờ ơ", 
        "khó tập trung", "tiêu cực", "khủng hoảng", "tuyệt vọng", "thất vọng", 
        "không vui", "buồn chán", "chán nản", "bất an", "hoang mang", "suy sụp", 
        "căng thẳng đầu óc", "stress học tập", "áp lực công việc", "pressure", 
        "burnout", "kiệt sức tinh thần", "tâm trạng", "mood swing", "tâm trạng thất thường", 
        "quá tải", "overwhelmed", "thất bại", "thất tình", "chia tay", "cô đơn", 
        "cô lập", "tổn thương", "hurt", "bị tổn thương", "buồn bã", "tủi thân", 
        "tự ti", "thiếu tự tin", "không tự tin", "low self-esteem", "insecure",
        "quá nhạy cảm", "nhạy cảm", "dễ xúc động", "hay khóc", "khóc", "buồn khóc",
        "không muốn sống", "không thấy vui", "không còn niềm vui", "không đam mê", 
        "mất đam mê", "mất đi hứng thú", "không biết phải làm gì", "hoang mang", 
        "không biết tương lai", "tương lai mờ mịt", "bối rối", "confused",
        // Thêm các từ khóa về mệt mỏi
        "tôi mệt quá", "tôi cảm thấy quá mệt", "mệt lắm", "mệt quá", "cảm thấy mệt",
        "mệt mỏi quá", "kiệt sức quá", "tôi kiệt sức", "quá mệt mỏi", "mệt rã rời",
        "không còn sức lực", "hết năng lượng", "mệt đứ đừ", "không muốn làm gì cả",
        "không có động lực", "không còn năng lượng", "không muốn thức dậy",
        "chẳng muốn làm gì", "không thiết tha", "không có hứng thú", "mất hết động lực",
        // Thêm các cách diễn đạt thông thường
        "tôi cảm thấy", "tôi thấy", "tôi đang", "tôi bị", "tôi hay", "tôi luôn", "tôi không thể",
        "tôi chán", "tôi buồn", "tôi lo", "tôi sợ", "tôi không biết phải làm sao",
        "tôi không hiểu", "tôi không muốn", "tôi ghét", "tôi khó", "tôi cần giúp đỡ",
        "giúp tôi", "cứu tôi", "tôi muốn chết", "tôi muốn biến mất", "tôi không muốn sống",
        "cuộc sống", "cuộc đời", "không còn ý nghĩa", "tôi đau", "khổ sở", "đau đớn", 
        "đau khổ quá", "sống không nổi", "quá căng thẳng", "quá áp lực", "không chịu nổi",
        "chịu không nổi", "quá sức", "tôi đuối sức", "hết chịu nổi", "bế tắc", "không lối thoát",
        "quẫn trí", "rối trí", "rối bời", "rối loạn", "bất ổn", "khó khăn", "vấn đề", "trăn trở",
        "thao thức", "không ngủ được", "mất ăn mất ngủ", "không muốn ăn", "biếng ăn",
        "chán ăn", "không muốn gặp ai", "không muốn nói chuyện", "thu mình lại",
        "tự cô lập", "xa lánh", "lánh đời", "tránh né", "sợ người", "sợ đám đông",
        "sợ tiếp xúc", "vô cảm", "chai sạn", "không còn cảm xúc", "cảm giác trống rỗng",
        "cảm thấy vô dụng", "vô dụng", "vô giá trị", "vô nghĩa", "kém cỏi", "tệ hại",
        "tôi thật tệ", "tôi thật dở", "tôi không xứng đáng", "không ai yêu tôi",
        "cô đơn quá", "tôi cô đơn", "không còn ai", "bị bỏ rơi", "bị phản bội", 
        "lẻ loi", "hiu quạnh", "trống vắng", "thiếu thốn", "khát khao", "khao khát",
        "thèm được yêu", "cần được yêu thương", "tôi cảm thấy tồi tệ", "tôi thấy buồn",
        "có ai hiểu tôi không", "tôi không còn thiết sống", "tôi không biết phải làm gì",
        "tôi không còn đường nào khác", "tôi không thấy hy vọng", "không còn hy vọng",
        "vô vọng", "tuyệt vọng", "tận cùng", "cùng cực", "khốn cùng", "bất lực", "bất lực",
        "bồn chồn", "căng thẳng", "áp lực", "stress", "không yên", "không thể thư giãn",
        
        // Thêm các từ khóa dạng câu hỏi và tìm kiếm lời khuyên
        "tôi nên", "tôi phải", "tôi cần", "làm gì", "làm sao", "như thế nào", 
        "bằng cách nào", "giúp đỡ", "tư vấn", "khuyên", "lời khuyên", "hướng dẫn",
        "nên làm gì", "phải làm gì", "cần làm gì", "làm thế nào", "làm cách nào",
        "có nên không", "có phải không", "có cần không", "nên hay không",
        "mình nên", "mình phải", "mình cần", "mình muốn", "tôi muốn",
        "tìm cách", "tìm hướng đi", "tìm lối thoát", "tìm giải pháp", "tìm hiểu",
        "có cách nào", "có phương pháp nào", "có ai giúp", "ai có thể giúp",
        "làm gì để", "làm sao để", "cách nào để", "phương pháp nào để",
        "mong muốn", "ước", "hy vọng", "mơ ước", "ao ước", "khát khao",
        "tại sao tôi", "vì sao tôi", "lý do nào", "nguyên nhân",
        "điều gì", "chuyện gì", "vấn đề gì", "lý do gì",
        "bao lâu", "khi nào", "lúc nào", "thời gian",
        "ở đâu", "nơi nào", "chỗ nào", "địa điểm",
        "ai biết", "ai hiểu", "ai từng", "ai đã",
        "kinh nghiệm", "trải nghiệm", "từng trải", "bài học",
        "thay đổi", "cải thiện", "nâng cao", "phát triển",
        
        // Thêm các từ khóa phiên bản không dấu
        "lo au", "tram cam", "cang thang", "buon", "lo lang", "so hai",
        "hoang loan", "co don", "tam ly", "tinh than", "suc khoe tam than", "tu tu",
        "ton thuong", "dau kho", "mat ngu", "cam xuc", "kho chiu", "tuc gian", 
        "gian du", "mat tap trung", "roi loan", "met moi", "kiet suc", "ap luc",
        "sang chan", "am anh", "suy nghi tieu cuc", 
        "dau dau", "nhuc dau", "dau bung", "chong mat", "mat dong luc", "tho o", 
        "kho tap trung", "tieu cuc", "khung hoang", "tuyet vong", "that vong", 
        "khong vui", "buon chan", "chan nan", "bat an", "hoang mang", "suy sup", 
        "cang thang dau oc", "stress hoc tap", "ap luc cong viec",
        "kiet suc tinh than", "tam trang", "tam trang that thuong", 
        "qua tai", "that bai", "that tinh", "chia tay", "co don", 
        "co lap", "ton thuong", "bi ton thuong", "buon ba", "tui than", 
        "tu ti", "thieu tu tin", "khong tu tin",
        "qua nhay cam", "nhay cam", "de xuc dong", "hay khoc", "khoc", "buon khoc",
        "khong muon song", "khong thay vui", "khong con niem vui", "khong dam me", 
        "mat dam me", "mat di hung thu", "khong biet phai lam gi", "hoang mang", 
        "khong biet tuong lai", "tuong lai mo mit", "boi roi",
        // Thêm các từ khóa về mệt mỏi (không dấu)
        "toi met qua", "toi cam thay qua met", "met lam", "met qua", "cam thay met",
        "met moi qua", "kiet suc qua", "toi kiet suc", "qua met moi", "met ra roi",
        "khong con suc luc", "het nang luong", "met du du", "khong muon lam gi ca",
        "khong co dong luc", "khong con nang luong", "khong muon thuc day",
        "chang muon lam gi", "khong thiet tha", "khong co hung thu", "mat het dong luc",
        // Thêm các cách diễn đạt thông thường (không dấu)
        "toi cam thay", "toi thay", "toi dang", "toi bi", "toi hay", "toi luon", "toi khong the",
        "toi chan", "toi buon", "toi lo", "toi so", "toi khong biet phai lam sao",
        "toi khong hieu", "toi khong muon", "toi ghet", "toi kho", "toi can giup do",
        "giup toi", "cuu toi", "toi muon chet", "toi muon bien mat", "toi khong muon song",
        "cuoc song", "cuoc doi", "khong con y nghia", "toi dau", "kho so", "dau don", 
        "dau kho qua", "song khong noi", "qua cang thang", "qua ap luc", "khong chiu noi",
        "chiu khong noi", "qua suc", "toi duoi suc", "het chiu noi", "be tac", "khong loi thoat",
        "quan tri", "roi tri", "roi boi", "roi loan", "bat on", "kho khan", "van de", "tran tro"
    );

    /**
     * Get a response for a question from the database if it exists
     * If not found, returns a default message
     */
    public String getResponse(String question) {
        return chatbotResponseRepository.findByQuestionContaining(question)
                .map(ChatbotResponse::getAnswer)
                .orElse("Xin lỗi, tôi không có câu trả lời cho câu hỏi này. Vui lòng thử hỏi điều gì đó khác!");
    }
    
    /**
     * Lọc thông tin cá nhân từ câu hỏi của người dùng
     * @param question Câu hỏi gốc
     * @return Câu hỏi đã được lọc bỏ thông tin cá nhân
     */
    private String filterPersonalInfo(String question) {
        if (question == null || question.trim().isEmpty()) {
            return question;
        }
        
        String filteredQuestion = question;
        
        // Lọc các pattern thông tin cá nhân
        for (Pattern pattern : PERSONAL_INFO_PATTERNS) {
            Matcher matcher = pattern.matcher(filteredQuestion);
            if (matcher.find()) {
                // Thay thế thông tin cá nhân bằng "[THÔNG TIN CÁ NHÂN]"
                filteredQuestion = matcher.replaceAll("[THÔNG TIN CÁ NHÂN]");
            }
        }
        
        // Lọc các địa điểm cụ thể
        for (String location : LOCATION_KEYWORDS) {
            // Tạo pattern với word boundary để tránh thay thế một phần của từ
            String regex = "\\b" + Pattern.quote(location) + "\\b";
            filteredQuestion = filteredQuestion.replaceAll(regex, "[ĐỊA ĐIỂM]");
        }
        
        // Kiểm tra nếu câu hỏi đã bị lọc hết (chỉ còn placeholder)
        if (filteredQuestion.trim().equals("[THÔNG TIN CÁ NHÂN]") || 
            filteredQuestion.trim().equals("[ĐỊA ĐIỂM]")) {
            return "Tôi đang gặp vấn đề tâm lý";
        }
        
        // Log thông tin debug
        if (!filteredQuestion.equals(question)) {
            System.out.println("Đã lọc thông tin cá nhân từ câu hỏi:");
            System.out.println("- Gốc: " + question);
            System.out.println("- Đã lọc: " + filteredQuestion);
        }
        
        return filteredQuestion;
    }
    
    /**
     * Get a response for a question, and if not found, ask an AI API
     * and save the question and answer to the database
     */
    public String getResponseAndSaveIfNew(String question) {
        // Lọc thông tin cá nhân trước khi xử lý
        String filteredQuestion = filterPersonalInfo(question);
        
        // Nếu câu hỏi đã bị lọc quá nhiều, trả về thông báo riêng
        if (filteredQuestion.trim().length() < 10 && !filteredQuestion.equals(question)) {
            return "Xin lỗi, tôi chỉ có thể trả lời các câu hỏi liên quan đến sức khỏe tâm lý và không thể xử lý thông tin cá nhân của bạn. Vui lòng chia sẻ vấn đề tâm lý bạn đang gặp phải mà không đề cập đến thông tin cá nhân cụ thể.";
        }
        
        // Check if we already have an answer in the database
        Optional<ChatbotResponse> existingResponse = chatbotResponseRepository.findByQuestionContaining(filteredQuestion);
        
        if (existingResponse.isPresent()) {
            return existingResponse.get().getAnswer();
        }
        
        // If not found, ask the AI API
        String aiResponse = getResponseFromGemini(filteredQuestion);
        
        // Save the filtered question and answer to the database for future use
        saveNewResponse(filteredQuestion, aiResponse);
        
        return aiResponse;
    }
    
    /**
     * Check if a question is related to mental health by looking for keywords
     */
    public boolean isMentalHealthRelated(String question) {
        String lowerQuestion = question.toLowerCase();
        
        // Check if any mental health keyword is present in the question
        return MENTAL_HEALTH_KEYWORDS.stream()
                .anyMatch(keyword -> lowerQuestion.contains(keyword.toLowerCase()));
    }
    
    /**
     * Get a response from Google Gemini API
     */
    private String getResponseFromGemini(String question) {
        // Câu hỏi đã được lọc thông tin cá nhân trước khi đến đây
        
        try {
            // Logging để gỡ lỗi
            System.out.println("--- Gọi getResponseFromGemini ---");
            System.out.println("Injected Gemini API Key (raw): " + geminiApiKey);
            System.out.println("Injected Gemini API Key (first 10 chars): " + (geminiApiKey != null && !geminiApiKey.isEmpty() ? geminiApiKey.substring(0, Math.min(10, geminiApiKey.length())) + "..." : "null or empty"));
            System.out.println("Gemini API Endpoint: " + geminiApiEndpoint);
            
            boolean isApiKeyMissingOrPlaceholder = geminiApiKey == null || geminiApiKey.isEmpty() || geminiApiKey.equals("your-key-here");
            System.out.println("Is API Key missing or placeholder? " + isApiKeyMissingOrPlaceholder);

            // Kiểm tra API key
            if (isApiKeyMissingOrPlaceholder) {
                System.out.println("API Key không hợp lệ hoặc là placeholder. Sử dụng simulated response.");
                return getSimulatedAIResponse(question);
            }
            
            System.out.println("API Key hợp lệ. Tiến hành gọi Gemini API.");
            
            // Chuẩn bị request URL với API key
            String requestUrl = geminiApiEndpoint + "?key=" + geminiApiKey;
            
            // Chuẩn bị header
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            // Chuẩn bị request body cho Gemini API
            Map<String, Object> requestBody = new HashMap<>();
            
            Map<String, Object> contents = new HashMap<>();
            
            // Thêm system prompt (role = USER trong Gemini API)
            List<Map<String, Object>> parts = new ArrayList<>();
            
            Map<String, Object> systemPart = new HashMap<>();
            systemPart.put("text", 
                "Bạn là một trợ lý tâm lý chuyên nghiệp, nhẹ nhàng và đồng cảm. " +
                "Chỉ trả lời các câu hỏi liên quan đến sức khỏe tâm lý. " +
                "Sử dụng phong cách trò chuyện thân thiện, gần gũi với người dùng. " +
                "Luôn bắt đầu bằng sự đồng cảm, thể hiện rằng bạn hiểu cảm giác của họ. " +
                "Sử dụng các câu hỏi cuối phản hồi để khuyến khích người dùng chia sẻ thêm. " +
                "Xưng 'mình' khi nói với người dùng. Nói với người dùng bằng 'bạn'. " +
                "Sử dụng ví dụ cụ thể và mô tả trực quan để người dùng thấy được bạn thực sự hiểu họ. " +
                "Ví dụ: 'Mình hiểu cảm giác đó, như thể mọi thứ đều đè nặng lên vai bạn phải không?' " +
                "Cung cấp 2-3 gợi ý thiết thực, cụ thể và ngắn gọn. " +
                "Nhắc nhở người dùng tìm kiếm sự trợ giúp chuyên môn nếu tình trạng nghiêm trọng. " +
                "Trả lời bằng tiếng Việt, rõ ràng và dễ hiểu."
            );
            
            parts.add(Map.of("text", "Hướng dẫn cho trợ lý: " + systemPart.get("text")));
            parts.add(Map.of("text", "Câu hỏi của người dùng: " + question));
            
            contents.put("role", "USER");
            contents.put("parts", parts);
            
            requestBody.put("contents", List.of(contents));
            
            // Thêm cấu hình generation
            Map<String, Object> generationConfig = new HashMap<>();
            generationConfig.put("temperature", 0.7);
            generationConfig.put("maxOutputTokens", 1500);
            requestBody.put("generationConfig", generationConfig);
            
            // Gửi request
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            ResponseEntity<Map> response = restTemplate.postForEntity(requestUrl, entity, Map.class);
            
            // Xử lý response từ Gemini API
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map<String, Object> responseBody = response.getBody();
                List<Map<String, Object>> candidates = (List<Map<String, Object>>) responseBody.get("candidates");
                
                if (candidates != null && !candidates.isEmpty()) {
                    Map<String, Object> candidate = candidates.get(0);
                    Map<String, Object> content = (Map<String, Object>) candidate.get("content");
                    
                    if (content != null) {
                        List<Map<String, Object>> responseParts = (List<Map<String, Object>>) content.get("parts");
                        if (responseParts != null && !responseParts.isEmpty()) {
                            return (String) responseParts.get(0).get("text");
                        }
                    }
                }
            }
            
            // Nếu không lấy được câu trả lời từ API
            return getSimulatedAIResponse(question);
            
        } catch (Exception e) {
            System.out.println("Lỗi kết nối API Gemini: " + e.getMessage());
            e.printStackTrace();
            System.out.println("Sử dụng simulated response do lỗi API.");
            return getSimulatedAIResponse(question);
        }
    }
    
    /**
     * Get a simulated AI response for testing when the real API is not available
     */
    private String getSimulatedAIResponse(String question) {
        String lowerQuestion = question.toLowerCase();
        
        // Trả về câu trả lời dựa trên từ khóa trong câu hỏi
        if (lowerQuestion.contains("lo âu") || lowerQuestion.contains("anxiety")) {
            return "Mình hiểu cảm giác lo âu đó thực sự khó chịu thế nào, nhất là khi nó cứ đeo bám mình không buông. Nhiều lúc tim đập nhanh, khó thở, cứ nghĩ mãi về những điều tồi tệ có thể xảy ra phải không?\n\n"
                 + "Những lúc như vậy, mình có thể thử:\n"
                 + "1. Hít thở sâu và chậm - hít vào từ từ đếm đến 4, giữ 2 giây, thở ra từ từ đếm đến 6. Cứ làm 5-10 lần như vậy.\n"
                 + "2. Thử kỹ thuật 5-4-3-2-1: nhìn thấy 5 vật, sờ 4 vật, nghe 3 âm thanh, ngửi 2 mùi, nếm 1 vị. Cách này giúp đưa mình về hiện tại.\n"
                 + "3. Nói chuyện với người thân hoặc bạn bè - đôi khi chỉ cần nói ra cũng giúp bớt lo lắng nhiều rồi.\n\n"
                 + "Bạn có muốn chia sẻ thêm về điều gì đang khiến bạn lo âu không? Mình sẽ lắng nghe.";
        } else if (lowerQuestion.contains("căng thẳng") || lowerQuestion.contains("stress")) {
            return "Mình hiểu cảm giác căng thẳng đó, như thể có quá nhiều thứ đè nặng lên vai và đầu óc mình cứ quay cuồng phải không? Đôi khi ngay cả những việc nhỏ cũng khiến mình thấy quá tải.\n\n"
                 + "Khi cảm thấy stress như vậy, bạn có thể:\n"
                 + "1. Dành cho mình 5-10 phút chỉ để thư giãn, không làm gì cả - ngồi yên, nhắm mắt và chỉ tập trung vào hơi thở.\n"
                 + "2. Viết ra giấy những gì đang làm bạn căng thẳng, rồi sắp xếp chúng theo thứ tự ưu tiên, những việc có thể hoãn lại.\n"
                 + "3. Thử tập thể dục nhẹ nhàng, như đi bộ 15 phút - việc di chuyển cơ thể giúp giải phóng endorphin tự nhiên.\n\n"
                 + "Bạn có muốn chia sẻ cụ thể hơn về những gì đang khiến bạn căng thẳng không? Mình ở đây để lắng nghe bạn.";
        } else if (lowerQuestion.contains("trầm cảm") || lowerQuestion.contains("depression")) {
            return "Mình thực sự hiểu cảm giác nặng nề đó, như thể mọi thứ đều mất màu sắc và không còn niềm vui trong những điều từng khiến bạn thích thú. Nhiều khi chỉ việc thức dậy thôi cũng đã là một nỗ lực lớn rồi phải không?\n\n"
                 + "Những điều có thể giúp bạn từng bước nhỏ:\n"
                 + "1. Đặt mục tiêu thật nhỏ mỗi ngày, chỉ cần là ra khỏi giường, tắm rửa, hoặc ăn một bữa đàng hoàng.\n"
                 + "2. Dành 10-15 phút phơi nắng sáng - ánh sáng tự nhiên có thể giúp cân bằng nhịp sinh học.\n"
                 + "3. Cố gắng kết nối với một người bạn hoặc người thân, chỉ cần tin nhắn ngắn cũng được.\n\n"
                 + "Bạn không cô đơn trong cảm giác này đâu. Mình ở đây nếu bạn cần ai đó lắng nghe. Bạn đã thử tìm sự hỗ trợ chuyên môn chưa?";
        } else if (lowerQuestion.contains("mất ngủ") || lowerQuestion.contains("insomnia") || lowerQuestion.contains("khó ngủ")) {
            return "Cảm giác nằm trằn trọc, nhìn đồng hồ và thấy thời gian trôi qua mà không thể chìm vào giấc ngủ thật khó chịu phải không bạn? Hoặc là cứ bị tỉnh giấc giữa đêm và không tài nào ngủ lại được.\n\n"
                 + "Một số điều có thể giúp bạn cải thiện giấc ngủ:\n"
                 + "1. Thử tạo một nghi thức đi ngủ: tắt thiết bị điện tử 30 phút trước khi ngủ, đọc sách nhẹ nhàng, uống trà thảo mộc (không caffeine).\n"
                 + "2. Giữ phòng ngủ mát mẻ, tối và yên tĩnh - thậm chí dùng máy tạo tiếng ồn trắng nếu cần.\n"
                 + "3. Thử kỹ thuật thư giãn cơ thể: bắt đầu từ ngón chân, dần dần thư giãn lên đến đỉnh đầu.\n\n"
                 + "Bạn đang gặp vấn đề gì với giấc ngủ vậy? Khó đi vào giấc ngủ hay thường xuyên tỉnh giấc giữa đêm?";
        } else if (lowerQuestion.contains("cô đơn") || lowerQuestion.contains("loneliness")) {
            return "Cảm giác cô đơn đó thật sự rất khó khăn, dù đôi khi ta có thể ở giữa rất nhiều người. Cảm giác như có một khoảng cách vô hình giữa mình và mọi người, phải không bạn?\n\n"
                 + "Một vài điều có thể giúp bạn kết nối lại:\n"
                 + "1. Thử bắt đầu với những cuộc trò chuyện nhỏ - dù chỉ là với người bán hàng hay người ngồi cạnh trên xe buýt.\n"
                 + "2. Tham gia vào hoạt động nhóm dựa trên sở thích của bạn - dù là đọc sách, nấu ăn hay thể thao.\n"
                 + "3. Tình nguyện viên - giúp đỡ người khác cũng là cách tuyệt vời để cảm thấy kết nối.\n\n"
                 + "Bạn có thể chia sẻ thêm về hoàn cảnh của mình không? Mình ở đây để lắng nghe.";
        } else if (lowerQuestion.contains("tức giận") || lowerQuestion.contains("giận dữ") || lowerQuestion.contains("anger")) {
            return "Mình hiểu cảm giác khi cơn giận dâng lên, như một ngọn lửa bùng phát mà ta khó kiểm soát. Nó có thể khiến ta nói hoặc làm những điều sau này hối tiếc phải không?\n\n"
                 + "Một vài cách có thể giúp bạn xử lý cơn giận:\n"
                 + "1. Tạm rời khỏi tình huống nếu có thể - nói đơn giản là \"Mình cần một chút thời gian để suy nghĩ\".\n"
                 + "2. Đếm ngược từ 10 và hít thở sâu trong khi đếm - cho phép não bộ có thời gian xử lý.\n"
                 + "3. Viết ra những gì bạn đang cảm thấy - đôi khi chỉ cần bày tỏ cảm xúc theo cách an toàn đã giúp giảm cường độ của nó.\n\n"
                 + "Bạn có thể chia sẻ điều gì đang khiến bạn tức giận không? Đôi khi nói ra cũng giúp ta nhìn nhận rõ hơn.";
        } else if (lowerQuestion.contains("đau đầu") || lowerQuestion.contains("nhức đầu")) {
            return "Mình hiểu cảm giác đau đầu đó thật khó chịu, như thể có ai đó đang siết chặt hoặc đóng đinh vào đầu mình vậy. Đau đầu thường xuyên có thể liên quan đến stress và căng thẳng tâm lý đấy.\n\n"
                 + "Một vài điều có thể giúp bạn:\n"
                 + "1. Thử kỹ thuật thư giãn như massage nhẹ nhàng vùng thái dương và gáy.\n"
                 + "2. Nghỉ ngơi trong phòng tối và yên tĩnh nếu có thể - ánh sáng và tiếng ồn có thể làm đau đầu nặng hơn.\n"
                 + "3. Uống nhiều nước - đôi khi đau đầu là dấu hiệu của việc cơ thể thiếu nước.\n\n"
                 + "Bạn thường bị đau đầu khi nào? Có liên quan đến thời điểm căng thẳng không? Có thể chia sẻ thêm không?";
        } else if (lowerQuestion.contains("mệt mỏi") || lowerQuestion.contains("kiệt sức")) {
            return "Mình hiểu cảm giác kiệt sức đó, như thể mọi việc đều đòi hỏi nỗ lực gấp đôi bình thường, và dù ngủ bao nhiêu cũng không thấy tỉnh táo phải không?\n\n"
                 + "Một vài điều có thể giúp bạn phục hồi năng lượng:\n"
                 + "1. Cho phép bản thân nghỉ ngơi thực sự - không chỉ ngủ mà còn là thời gian không làm gì cả, không có mục tiêu hay kỳ vọng.\n"
                 + "2. Kiểm tra lại thói quen sinh hoạt - ngủ đủ giấc, ăn uống cân bằng, vận động nhẹ nhàng.\n"
                 + "3. Học cách nói 'không' với những việc không thực sự cần thiết - đôi khi ta cần đặt ranh giới rõ ràng hơn.\n\n"
                 + "Bạn có muốn chia sẻ thêm về những gì đang khiến bạn cảm thấy kiệt sức không? Mình ở đây để lắng nghe.";
        } else if (lowerQuestion.contains("chán nản") || lowerQuestion.contains("buồn chán")) {
            return "Mình hiểu cảm giác chán nản đó, như thể không có gì thú vị hay đáng mong đợi, ngày nào cũng như ngày nào phải không?\n\n"
                 + "Một vài ý tưởng có thể giúp bạn tìm lại niềm vui:\n"
                 + "1. Thử một điều hoàn toàn mới mẻ - một sở thích, một món ăn, một con đường đi dạo mà bạn chưa từng thử.\n"
                 + "2. Kết nối lại với những điều từng khiến bạn hứng thú trước đây - đôi khi ta chỉ cần nhắc nhở bản thân về những niềm vui đơn giản.\n"
                 + "3. Thay đổi nhỏ trong thói quen hàng ngày - đôi khi chỉ cần phá vỡ sự đơn điệu cũng tạo ra khác biệt lớn.\n\n"
                 + "Bạn có thể chia sẻ điều gì khiến bạn cảm thấy vui vẻ trước đây không? Đôi khi nhìn lại có thể giúp ta tìm hướng đi mới.";
        } else if (lowerQuestion.contains("áp lực") || lowerQuestion.contains("quá tải")) {
            return "Mình hiểu cảm giác đó... khi mọi thứ dồn dập quá nhiều, như thể bạn đang cố giữ thăng bằng với hàng chục quả bóng cùng lúc. Cảm giác quá tải đó thật sự rất kiệt sức phải không?\n\n"
                 + "Một vài điều có thể giúp bạn vượt qua:\n"
                 + "1. Phân chia mọi thứ thành những phần nhỏ hơn, dễ quản lý hơn - tập trung vào từng bước một thay vì toàn bộ con đường.\n"
                 + "2. Xác định những việc thực sự quan trọng và những việc có thể đợi - đôi khi ta cần học cách ưu tiên.\n"
                 + "3. Dành thời gian cho bản thân mỗi ngày, dù chỉ 15 phút - uống trà, đi dạo, hoặc chỉ đơn giản là ngồi yên.\n\n"
                 + "Bạn đang đối mặt với áp lực từ đâu vậy? Công việc, học tập, hay các mối quan hệ? Chia sẻ nhiều hơn nếu bạn muốn.";
        } else if (lowerQuestion.contains("tự ti") || lowerQuestion.contains("thiếu tự tin")) {
            return "Mình hiểu cảm giác tự ti đó, như thể mình không đủ tốt so với người khác, hoặc luôn lo lắng về những gì người khác nghĩ về mình. Đôi khi nó làm ta không dám thử những điều mới phải không?\n\n"
                 + "Một vài điều có thể giúp bạn xây dựng sự tự tin:\n"
                 + "1. Ghi nhận những thành công nhỏ của bản thân mỗi ngày - dù chỉ là việc nhỏ cũng đáng được công nhận.\n"
                 + "2. Thực hành nói chuyện với bản thân một cách tích cực - thay vì \"Mình làm không được\" thì nói \"Mình sẽ cố gắng hết sức\".\n"
                 + "3. Nhớ rằng mọi người đều có điểm mạnh và điểm yếu khác nhau - hãy tập trung vào điểm mạnh của bạn.\n\n"
                 + "Bạn có thể chia sẻ điểm mạnh của mình là gì không? Đôi khi ta quên mất những điều tuyệt vời về bản thân.";
        } else {
            return "Cảm ơn bạn đã chia sẻ với mình. Mình thấy bạn đang quan tâm đến vấn đề sức khỏe tâm lý, và mình rất muốn hỗ trợ bạn nhiều hơn.\n\n"
                 + "Để mình có thể hiểu rõ hơn về tình trạng của bạn, bạn có thể chia sẻ thêm về:\n"
                 + "1. Cảm xúc hoặc triệu chứng cụ thể mà bạn đang trải qua?\n"
                 + "2. Những thay đổi nào trong cuộc sống gần đây có thể ảnh hưởng đến bạn?\n"
                 + "3. Bạn đã thử cách nào để cải thiện tình trạng chưa?\n\n"
                 + "Mình ở đây để lắng nghe và hỗ trợ bạn. Đôi khi chỉ cần nói ra những gì đang cảm thấy cũng giúp ta nhẹ nhõm hơn nhiều rồi.";
        }
    }
    
    /**
     * Save a new question and answer to the database
     */
    private void saveNewResponse(String question, String answer) {
        ChatbotResponse chatbotResponse = new ChatbotResponse();
        chatbotResponse.setQuestion(question);
        chatbotResponse.setAnswer(answer);
        chatbotResponseRepository.save(chatbotResponse);
    }
}
