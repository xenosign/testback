package com.tetz.testback.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;

@RestController
@CrossOrigin("*")
@RequestMapping("/")
public class HomeController {
    @GetMapping("")
    public String home() {
        return "Hello, Tetz's test server";
    }
    
    @GetMapping("/test")
    public ResponseEntity<Map<String, String>> test() {
        Map<String, String> response = new HashMap<>();
        response.put("message", "여기는 Tetz 백엔드 서버 입니다");
        return ResponseEntity.ok(response);
    }
}
