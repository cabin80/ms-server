package com.ms.admin.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AdminPageController {

    @GetMapping("/admin")
    public String adminPage() {
        return "forward:/admin.html";
    }

    @GetMapping("/")
    public String indexPage() {
        return "forward:/index.html";
    }
}
