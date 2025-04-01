package com.tetz.testback.controller;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@CrossOrigin("*")
@RequestMapping("/todos")
public class TodoController {
    private static final List<Map<String, Object>> ALL_TODOS = new ArrayList<>();
    private static final List<Map<String, String>> ALL_USERS = new ArrayList<>();

    static {
        // 초기 Todo 데이터 추가
        addTodo("1", "첫 과제 제출 하기", "이거 어떻게 하는거죠!?", false);
        addTodo("2", "페이커 숭배하기", "여러분 숭배할 시간입니다. 진짜 이번 우승 까지는 안바랍니다! ㅎㅎㅎㅎ", false);
        addTodo("3", "Vue 학습", "Vue 부수기", false);
        addTodo("4", "코테 정복", "취업용 코테 까지만 퐈이팅!!", false);

        // 사용자 데이터 추가
        addUser("12", "12");
    }

    // Todo 추가 헬퍼 메소드
    private static void addTodo(String id, String todo, String desc, boolean done) {
        Map<String, Object> todoItem = new HashMap<>();
        todoItem.put("id", id);
        todoItem.put("todo", todo);
        todoItem.put("desc", desc);
        todoItem.put("done", done);
        ALL_TODOS.add(todoItem);
    }

    // 사용자 추가 헬퍼 메소드
    private static void addUser(String id, String password) {
        Map<String, String> user = new HashMap<>();
        user.put("id", id);
        user.put("password", password);
        ALL_USERS.add(user);
    }

    // 모든 Todo 항목 반환
    @GetMapping("")
    public List<Map<String, Object>> getAllTodos() {
        return ALL_TODOS;
    }
}