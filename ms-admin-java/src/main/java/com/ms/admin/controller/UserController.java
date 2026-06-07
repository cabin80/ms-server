package com.ms.admin.controller;

import com.ms.admin.config.Result;
import com.ms.admin.entity.User;
import com.ms.admin.service.AdminService;
import org.springframework.web.bind.annotation.*;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final AdminService adminService;

    public UserController(AdminService adminService) {
        this.adminService = adminService;
    }

    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        String password = body.get("password");
        if (username == null || password == null) {
            return Result.error(400, "用户名或密码不能为空");
        }
        User user = adminService.login(username, md5(password));
        if (user == null) {
            return Result.error(401, "用户名或密码错误");
        }
        return Result.success(user, "登录成功");
    }

    @PostMapping("/register")
    public Map<String, Object> register(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        String password = body.get("password");
        String nickname = body.get("nickname");
        if (username == null || password == null) {
            return Result.error(400, "用户名或密码不能为空");
        }
        try {
            User user = adminService.register(username, md5(password), nickname != null ? nickname : username);
            return Result.success(Map.of("id", user.getId()), "注册成功");
        } catch (IllegalArgumentException e) {
            return Result.error(400, "用户名已存在");
        }
    }

    @GetMapping("/{id}")
    public Map<String, Object> getUser(@PathVariable Integer id) {
        User user = adminService.getUserById(id);
        if (user == null) return Result.error(404, "Not found");
        return Result.success(user);
    }

    private String md5(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(input.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("MD5 not available", e);
        }
    }
}
