package net.javaguides.springboot.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import net.javaguides.springboot.model.Food;
import net.javaguides.springboot.repository.FoodRepository;

@WebMvcTest(FoodController.class)
class FoodControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private FoodRepository foodRepository;

	@Test
	void getFoodById_returns200_whenFound() throws Exception {
		Food food = new Food();
		food.setId(1L);
		food.setName("Burger");
		food.setDescription("Beef burger");
		food.setPrice(9.99);
		food.setImageUrl("img.jpg");

		when(foodRepository.findById(1L)).thenReturn(Optional.of(food));

		mockMvc.perform(get("/api/v1/foods/1"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(1))
				.andExpect(jsonPath("$.name").value("Burger"));
	}

	@Test
	void getFoodById_returns404_whenMissing() throws Exception {
		when(foodRepository.findById(99L)).thenReturn(Optional.empty());

		mockMvc.perform(get("/api/v1/foods/99")).andExpect(status().isNotFound());
	}

	@Test
	void createFood_returns200_andBody() throws Exception {
		Food input = new Food();
		input.setName("Pizza");
		input.setDescription("Cheese");
		input.setPrice(12.5);
		input.setImageUrl("pizza.jpg");

		Food saved = new Food();
		saved.setId(10L);
		saved.setName(input.getName());
		saved.setDescription(input.getDescription());
		saved.setPrice(input.getPrice());
		saved.setImageUrl(input.getImageUrl());

		when(foodRepository.save(any(Food.class))).thenReturn(saved);

		mockMvc.perform(post("/api/v1/foods")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"name\":\"Pizza\",\"description\":\"Cheese\",\"price\":12.5,\"imageUrl\":\"pizza.jpg\"}"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(10))
				.andExpect(jsonPath("$.name").value("Pizza"));
	}
}
