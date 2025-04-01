package com.tetz.testback.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/cors/off")
public class CorsOffController {
    @GetMapping("")
    public ResponseEntity<Map<String, String>> test() {
        Map<String, String> response = new HashMap<>();
        response.put("message", "CORS 서버 설정이 안되어 있는 END POINT 입니다!");
        return ResponseEntity.ok(response);
    }
}
