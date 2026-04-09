package net.javaguides.springboot.controller;

import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import net.javaguides.springboot.model.Food;
import net.javaguides.springboot.service.FoodService;

@RestController
@RequestMapping("/api/v1/")
public class FoodController {

    private final FoodService foodService;

    public FoodController(FoodService foodService) {
        this.foodService = foodService;
    }

    @GetMapping("/foods")
    public List<Food> getAllFoods() {
        return foodService.getAllFoods();
    }

    @GetMapping("/foods/{id}")
    public Food getFoodById(@PathVariable Long id) {
        return foodService.getFoodById(id);
    }

    @PostMapping("/foods")
    public Food createFood(@RequestBody Food food) {
        return foodService.createFood(food);
    }

    @PutMapping("/foods/{id}")
    public Food updateFood(@PathVariable Long id, @RequestBody Food foodDetails) {
        return foodService.updateFood(id, foodDetails);
    }

    @DeleteMapping("/foods/{id}")
    public Map<String, Boolean> deleteFood(@PathVariable Long id) {
        foodService.deleteFood(id);
        return Map.of("deleted", Boolean.TRUE);
    }
}
