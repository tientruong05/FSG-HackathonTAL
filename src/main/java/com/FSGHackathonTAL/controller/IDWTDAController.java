package com.FSGHackathonTAL.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Controller để xử lý các yêu cầu liên quan đến trang "chill mode".
 */
@Controller
public class IDWTDAController {

    /**
     * Xử lý yêu cầu GET cho trang "chill mode".
     * @return Tên view "IDWTDA" để hiển thị trang.
     */
    @GetMapping("/chill-mode")
    public String getRelaxPage() {
        return "IDWTDA";
    }
}
