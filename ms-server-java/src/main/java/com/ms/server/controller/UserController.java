package com.ms.server.controller;

import com.ms.server.config.Result;
import com.ms.server.entity.User;
import com.ms.server.service.MusicService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final MusicService musicService;

    public UserController(MusicService musicService) {
        this.musicService = musicService;
    }

    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody Map<String, String> body) {
        var user = musicService.login(body.get("username"), body.get("password"));
        if (user == null) return Result.error(401, "用户名或密码错误");
        return Result.success(user, "登录成功");
    }

    @PostMapping("/register")
    public Map<String, Object> register(@RequestBody Map<String, String> body) {
        User user = musicService.register(body.get("username"), body.get("password"), body.get("nickname"));
        if (user == null) return Result.error(400, "用户名已存在");
        return Result.success(Map.of("id", user.getId()), "注册成功");
    }

    @GetMapping("/{id}")
    public Map<String, Object> getById(@PathVariable Integer id) {
        User user = musicService.getUserById(id);
        if (user == null) return Result.error(404, "Not found");
        Map<String, Object> map = Map.of(
            "id", user.getId(),
            "username", user.getUsername(),
            "nickname", user.getNickname(),
            "avatar", user.getAvatar()
        );
        return Result.success(map);
    }
}
