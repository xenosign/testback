package com.tetz.testback.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

@RestController
@CrossOrigin(origins = "*", allowedHeaders = "Origin, X-Requested-With, Content-Type, Accept", methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.OPTIONS})
public class FormController {

    // GET 요청 처리
    @GetMapping("/form")
    public ResponseEntity<?> handleGetForm(@RequestParam Map<String, String> queryParams,
                                           HttpServletRequest request) {
        System.out.println("\n===== GET 요청이 들어왔습니다 =====");
        System.out.println("- 시간: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

        // 1. 요청 URL 및 메소드
        System.out.println("\n[기본 요청 정보]");
        System.out.println("- URL: " + request.getRequestURI() + (request.getQueryString() != null ? "?" + request.getQueryString() : ""));
        System.out.println("- Method: " + request.getMethod());

        // 2. 요청 헤더
        System.out.println("\n[요청 헤더]");
        Map<String, String> headers = new HashMap<>();
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            headers.put(headerName, request.getHeader(headerName));
        }
        System.out.println(headers);

        // 3. 클라이언트 정보
        System.out.println("\n[클라이언트 정보]");
        System.out.println("- IP: " + request.getRemoteAddr());
        System.out.println("- User-Agent: " + request.getHeader("User-Agent"));

        // 4. 쿼리 파라미터
        System.out.println("\n[쿼리 파라미터]");
        System.out.println(queryParams);

        // 응답 데이터 구성
        Map<String, Object> response = new HashMap<>();
        response.put("message", "GET 요청을 받았습니다");
        response.put("receivedData", queryParams);

        return ResponseEntity.ok(response);
    }

    // POST 요청 처리 - JSON 형식
    @PostMapping(value = "/form", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> handlePostJsonForm(@RequestBody Map<String, Object> body,
                                                HttpServletRequest request) {
        return processPostRequest(body, request);
    }

    // POST 요청 처리 - form-urlencoded 형식
    @PostMapping(value = "/form", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public ResponseEntity<?> handlePostUrlEncodedForm(@RequestParam Map<String, String> formData,
                                                      HttpServletRequest request) {
        Map<String, Object> body = new HashMap<>(formData);
        return processPostRequest(body, request);
    }

    // POST 요청 공통 처리 메서드
    private ResponseEntity<?> processPostRequest(Map<String, Object> body, HttpServletRequest request) {
        System.out.println("\n===== POST 요청이 들어왔습니다 =====");
        System.out.println("- 시간: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

        // 1. 요청 URL 및 메소드
        System.out.println("\n[기본 요청 정보]");
        System.out.println("- URL: " + request.getRequestURI() + (request.getQueryString() != null ? "?" + request.getQueryString() : ""));
        System.out.println("- Method: " + request.getMethod());

        // 2. 요청 헤더
        System.out.println("\n[요청 헤더]");
        Map<String, String> headers = new HashMap<>();
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            headers.put(headerName, request.getHeader(headerName));
        }
        System.out.println(headers);

        // 3. 클라이언트 정보
        System.out.println("\n[클라이언트 정보]");
        System.out.println("- IP: " + request.getRemoteAddr());
        System.out.println("- User-Agent: " + request.getHeader("User-Agent"));

        // 4. 요청 바디 (폼 데이터)
        System.out.println("\n[요청 바디]");
        System.out.println(body);

        // 응답 데이터 구성
        Map<String, Object> response = new HashMap<>();
        response.put("message", "POST 요청을 받았습니다");
        response.put("receivedData", body);

        return ResponseEntity.ok(response);
    }

    // 모든 Content-Type을 허용하는 폴백 핸들러
    @PostMapping(value = "/form", consumes = MediaType.ALL_VALUE)
    public ResponseEntity<?> handlePostFormFallback(HttpServletRequest request) {
        System.out.println("\n===== 지원되지 않는 Content-Type POST 요청이 들어왔습니다 =====");
        System.out.println("- Content-Type: " + request.getContentType());

        // 파라미터 추출 시도
        Map<String, String> allParams = new HashMap<>();
        request.getParameterMap().forEach((key, values) -> {
            if (values != null && values.length > 0) {
                allParams.put(key, values[0]);
            }
        });

        return processPostRequest(new HashMap<>(allParams), request);
    }

    // OPTIONS 요청 처리 (CORS preflight 요청)
    @RequestMapping(value = "/form", method = RequestMethod.OPTIONS)
    public ResponseEntity<?> handleOptions() {
        return ResponseEntity.ok().build();
    }
}