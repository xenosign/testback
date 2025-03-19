package com.tetz.testback.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin("*")
@RequestMapping("/")
public class HomeController {
    @GetMapping("")
    public String home() {
        return "Hello, Tetz's test server";
    }
    
    @GetMapping("/test")
    public ResponseEntity<String> test() {
        return ResponseEntity.ok("여기는 Tetz 백엔드 서버 입니다");
    }
}
