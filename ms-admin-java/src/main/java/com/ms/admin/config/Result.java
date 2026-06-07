package com.ms.admin.config;

import java.util.HashMap;
import java.util.Map;

public class Result {
    public static Map<String, Object> success(Object data) {
        Map<String, Object> map = new HashMap<>();
        map.put("code", 200);
        map.put("data", data);
        return map;
    }

    public static Map<String, Object> success(Object data, String message) {
        Map<String, Object> map = new HashMap<>();
        map.put("code", 200);
        map.put("data", data);
        map.put("message", message);
        return map;
    }

    public static Map<String, Object> error(int code, String message) {
        Map<String, Object> map = new HashMap<>();
        map.put("code", code);
        map.put("message", message);
        return map;
    }
}
