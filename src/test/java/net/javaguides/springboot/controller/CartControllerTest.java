package net.javaguides.springboot.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import net.javaguides.springboot.model.Cart;
import net.javaguides.springboot.model.Food;
import net.javaguides.springboot.service.CartService;

@WebMvcTest(CartController.class)
class CartControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private CartService cartService;

	@Test
	void getCartItemById_returns200_whenFound() throws Exception {
		Food food = food(1L, "Burger");
		Cart cart = cart(5L, food, 2);

		when(cartService.getCartItemById(5L)).thenReturn(cart);

		mockMvc.perform(get("/api/v1/carts/5"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(5))
				.andExpect(jsonPath("$.quantity").value(2))
				.andExpect(jsonPath("$.food.id").value(1))
				.andExpect(jsonPath("$.food.name").value("Burger"));
	}

	@Test
	void getCartItemById_returns404_whenMissing() throws Exception {
		when(cartService.getCartItemById(99L))
				.thenThrow(new net.javaguides.springboot.exception.ResourceNotFoundException("Cart item not found with id: 99"));

		mockMvc.perform(get("/api/v1/carts/99")).andExpect(status().isNotFound());
	}

	@Test
	void getAllCartItems_returns200_andList() throws Exception {
		Food food = food(1L, "Burger");
		when(cartService.getAllCartItems()).thenReturn(List.of(cart(1L, food, 1)));

		mockMvc.perform(get("/api/v1/carts"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].id").value(1))
				.andExpect(jsonPath("$[0].quantity").value(1));
	}

	@Test
	void createCartItem_returns200_andBody() throws Exception {
		Food food = food(1L, "Burger");
		Cart saved = cart(10L, food, 2);
		when(cartService.createCartItem(any(Cart.class))).thenReturn(saved);

		mockMvc.perform(post("/api/v1/carts")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"food\":{\"id\":1,\"name\":\"Burger\",\"description\":\"d\",\"price\":9.99,\"imageUrl\":\"i.jpg\"},\"quantity\":2}"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(10))
				.andExpect(jsonPath("$.quantity").value(2))
				.andExpect(jsonPath("$.food.id").value(1));
	}

	@Test
	void createCartItem_defaultsQuantityToOne_whenZero() throws Exception {
		Food food = food(1L, "Burger");
		Cart saved = cart(11L, food, 1);
		when(cartService.createCartItem(any(Cart.class))).thenReturn(saved);

		mockMvc.perform(post("/api/v1/carts")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"food\":{\"id\":1,\"name\":\"Burger\",\"description\":\"d\",\"price\":9.99,\"imageUrl\":\"i.jpg\"},\"quantity\":0}"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.quantity").value(1));
	}

	@Test
	void createCartItem_returns404_whenFoodMissing() throws Exception {
		when(cartService.createCartItem(any(Cart.class)))
				.thenThrow(new net.javaguides.springboot.exception.ResourceNotFoundException("Food not found with id: 42"));

		mockMvc.perform(post("/api/v1/carts")
				.contentType(MediaType.APPLICATION_JSON)
				.content(
						"{\"food\":{\"id\":42,\"name\":\"X\",\"description\":\"d\",\"price\":1,\"imageUrl\":\"x.jpg\"},\"quantity\":1}"))
				.andExpect(status().isNotFound());
	}

	@Test
	void createCartItem_returns404_whenFoodIdInvalid() throws Exception {
		when(cartService.createCartItem(any(Cart.class)))
				.thenThrow(new net.javaguides.springboot.exception.ResourceNotFoundException("Valid food id is required"));
		mockMvc.perform(post("/api/v1/carts")
				.contentType(MediaType.APPLICATION_JSON)
				.content(
						"{\"food\":{\"id\":0,\"name\":\"X\",\"description\":\"d\",\"price\":1,\"imageUrl\":\"x.jpg\"},\"quantity\":1}"))
				.andExpect(status().isNotFound());
	}

	@Test
	void updateCartItem_returns200_whenFound() throws Exception {
		Food food = food(1L, "Burger");
		Cart updated = cart(7L, food, 3);
		when(cartService.updateCartItem(any(Long.class), any(Cart.class))).thenReturn(updated);

		mockMvc.perform(put("/api/v1/carts/7")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"quantity\":3}"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(7))
				.andExpect(jsonPath("$.quantity").value(3));
	}

	@Test
	void updateCartItem_returns404_whenCartMissing() throws Exception {
		when(cartService.updateCartItem(any(Long.class), any(Cart.class)))
				.thenThrow(new net.javaguides.springboot.exception.ResourceNotFoundException("Cart item not found with id: 7"));

		mockMvc.perform(put("/api/v1/carts/7")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"quantity\":3}"))
				.andExpect(status().isNotFound());
	}

	@Test
	void deleteCartItem_returns200_andDeletedFlag() throws Exception {
		doNothing().when(cartService).deleteCartItem(8L);

		mockMvc.perform(delete("/api/v1/carts/8"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.deleted").value(true));
	}

	@Test
	void deleteCartItem_returns404_whenMissing() throws Exception {
		doThrow(new net.javaguides.springboot.exception.ResourceNotFoundException("Cart item not found with id: 8"))
				.when(cartService).deleteCartItem(8L);

		mockMvc.perform(delete("/api/v1/carts/8")).andExpect(status().isNotFound());
	}

	private static Food food(long id, String name) {
		Food f = new Food();
		f.setId(id);
		f.setName(name);
		f.setDescription("desc");
		f.setPrice(9.99);
		f.setImageUrl("img.jpg");
		return f;
	}

	private static Cart cart(long id, Food food, int quantity) {
		Cart c = new Cart();
		c.setId(id);
		c.setFood(food);
		c.setQuantity(quantity);
		return c;
	}
}
