package com.tetz.testback.controller;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@CrossOrigin("*")
@RequestMapping("/food")
public class FoodController {
    private static final List<Map<String, String>> ALL_FOODS = new ArrayList<>();

    static {
        // 한식 추가
        addFood("korean", "제육");
        addFood("korean", "돈까스");
        addFood("korean", "광어회");

        // 중식 추가
        addFood("chinese", "탄탄면");
        addFood("chinese", "어향동고");
        addFood("chinese", "마파두부");

        // 일식 추가
        addFood("japanese", "초밥");
        addFood("japanese", "라멘");
        addFood("japanese", "타코야끼");
    }

    // 음식 추가 헬퍼 메소드
    private static void addFood(String category, String foodName) {
        Map<String, String> food = new HashMap<>();
        food.put("category", category);
        food.put("food", foodName);
        ALL_FOODS.add(food);
    }

    // 모든 카테고리의 음식 정보를 반환
    @GetMapping("/all")
    public List<Map<String, String>> all() {
        return ALL_FOODS;
    }

    // 한식만 반환
    @GetMapping("/korean")
    public List<Map<String, String>> korean() {
        return filterFoodsByCategory("korean");
    }

    // 중식만 반환
    @GetMapping("/chinese")
    public List<Map<String, String>> chinese() {
        return filterFoodsByCategory("chinese");
    }

    // 일식만 반환
    @GetMapping("/japanese")
    public List<Map<String, String>> japanese() {
        return filterFoodsByCategory("japanese");
    }

    // 카테고리별 음식 필터링 헬퍼 메소드
    private List<Map<String, String>> filterFoodsByCategory(String category) {
        return ALL_FOODS.stream()
                .filter(food -> category.equals(food.get("category")))
                .map(food -> {
                    Map<String, String> simplifiedFood = new HashMap<>();
                    simplifiedFood.put("food", food.get("food"));
                    return simplifiedFood;
                })
                .collect(Collectors.toList());
    }
}