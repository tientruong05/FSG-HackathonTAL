package com.FSGHackathonTAL.service;

import com.FSGHackathonTAL.entity.User;
import com.FSGHackathonTAL.entity.ChatSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class DoctorAvailabilityService {

    @Autowired
    private UserService userService;

    @Autowired
    private ChatSessionService chatSessionService;

    /**
     * Lấy danh sách bác sĩ đang online và sẵn sàng (không trong phiên chat)
     * @return List<User> Danh sách bác sĩ sẵn sàng
     */
    public List<User> getAvailableDoctors() {
        try {
            List<User> onlineDoctors = userService.getOnlineDoctors();
            if (onlineDoctors == null || onlineDoctors.isEmpty()) {
                return List.of();
            }

            return onlineDoctors.stream()
                    .filter(doctor -> {
                        try {
                            // Kiểm tra bác sĩ có online không
                            if (!doctor.getIsOnline()) {
                                return false;
                            }
                            
                            // Kiểm tra bác sĩ có đang bận chat không
                            Optional<ChatSession> activeChat = chatSessionService.getDoctorActiveChat(doctor);
                            return activeChat.isEmpty(); // Chỉ lấy bác sĩ không có phiên chat
                        } catch (Exception e) {
                            System.err.println("Error checking doctor availability: " + e.getMessage());
                            return false;
                        }
                    })
                    .collect(Collectors.toList());
        } catch (Exception e) {
            System.err.println("Error in getAvailableDoctors: " + e.getMessage());
            return List.of();
        }
    }
    
    /**
     * Lấy danh sách bác sĩ đang online nhưng bận (trong phiên chat)
     * @return List<User> Danh sách bác sĩ đang bận
     */
    public List<User> getBusyDoctors() {
        try {
            List<User> onlineDoctors = userService.getOnlineDoctors();
            if (onlineDoctors == null || onlineDoctors.isEmpty()) {
                return List.of();
            }

            return onlineDoctors.stream()
                    .filter(doctor -> {
                        try {
                            // Kiểm tra bác sĩ có online không
                            if (!doctor.getIsOnline()) {
                                return false;
                            }
                            
                            // Kiểm tra bác sĩ có đang bận chat không
                            Optional<ChatSession> activeChat = chatSessionService.getDoctorActiveChat(doctor);
                            return activeChat.isPresent(); // Chỉ lấy bác sĩ đang có phiên chat
                        } catch (Exception e) {
                            System.err.println("Error checking doctor busy status: " + e.getMessage());
                            return false;
                        }
                    })
                    .collect(Collectors.toList());
        } catch (Exception e) {
            System.err.println("Error in getBusyDoctors: " + e.getMessage());
            return List.of();
        }
    }
    
    /**
     * Lấy danh sách tất cả bác sĩ online, phân loại thành sẵn sàng và bận
     * @return List<List<User>> Mảng chứa [danh sách bác sĩ sẵn sàng, danh sách bác sĩ bận]
     */
    public List<List<User>> getAllOnlineDoctorsByStatus() {
        List<List<User>> result = new ArrayList<>();
        result.add(getAvailableDoctors());
        result.add(getBusyDoctors());
        return result;
    }
}
