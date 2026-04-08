package net.javaguides.springboot.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import net.javaguides.springboot.model.Food;
import net.javaguides.springboot.repository.FoodRepository;

@RestController
@RequestMapping("/api/v1/")
public class FoodController {

    @Autowired
    private FoodRepository foodRepository;

    // get all employees

    @GetMapping("/foods")
    public List<Food> getAllFoods() {
        return foodRepository.findAll();
    }
}
