package net.javaguides.springboot.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import net.javaguides.springboot.model.Food;
import net.javaguides.springboot.repository.FoodRepository;



@RestController
@RequestMapping("/api/v1/")
public class FoodController {

    @Autowired
    private FoodRepository foodRepository;

    // get all foods
    @GetMapping("/foods")
    public List<Food> getAllFoods() {
        return foodRepository.findAll();
    }

    // get foods by id
    @GetMapping("/foods/{id}")
    public Food getFoodById(@PathVariable Long id) {
        Optional<Food> food = foodRepository.findById(id);
        return food.orElseThrow(() -> new RuntimeException("Food not found"));
    }
    
    // create food
    @PostMapping("/foods")
    public Food createFood(@RequestBody Food food) {
        return foodRepository.save(food);
    }
}
