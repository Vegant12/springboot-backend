package net.javaguides.springboot.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import net.javaguides.springboot.model.Food;
import net.javaguides.springboot.model.Order;
import net.javaguides.springboot.model.OrderItem;
import net.javaguides.springboot.repository.FoodRepository;
import net.javaguides.springboot.repository.OrderItemRepository;
import net.javaguides.springboot.repository.OrderRepository;

@WebMvcTest(OrderItemController.class)
class OrderItemControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private OrderItemRepository orderItemRepository;

	@MockitoBean
	private OrderRepository orderRepository;

	@MockitoBean
	private FoodRepository foodRepository;

	@Test
	void getOrderItems_returnsAll_whenNoOrderId() throws Exception {
		Order parentOrder = order(1L, "PENDING");
		Food burger = food(1L, "Burger", 10.0);
		OrderItem item = orderItem(1L, parentOrder, burger, 2, 10.0);

		when(orderItemRepository.findAll()).thenReturn(List.of(item));

		mockMvc.perform(get("/api/v1/order-items"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].id").value(1))
				.andExpect(jsonPath("$[0].quantity").value(2))
				.andExpect(jsonPath("$[0].price").value(10.0))
				.andExpect(jsonPath("$[0].food.id").value(1))
				.andExpect(jsonPath("$[0].food.name").value("Burger"));
	}

	@Test
	void getOrderItems_filtersByOrderId_whenParamPresent() throws Exception {
		Order parentOrder = order(5L, "PENDING");
		Food burger = food(1L, "Burger", 9.99);
		OrderItem item = orderItem(2L, parentOrder, burger, 1, 9.99);

		when(orderItemRepository.findByOrderId(5L)).thenReturn(List.of(item));

		mockMvc.perform(get("/api/v1/order-items").param("orderId", "5"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].id").value(2))
				.andExpect(jsonPath("$[0].quantity").value(1));
	}

	@Test
	void getOrderItemById_returns200_whenFound() throws Exception {
		Order parentOrder = order(1L, "PENDING");
		Food burger = food(1L, "Burger", 10.0);
		OrderItem item = orderItem(7L, parentOrder, burger, 2, 10.0);

		when(orderItemRepository.findById(7L)).thenReturn(Optional.of(item));

		mockMvc.perform(get("/api/v1/order-items/7"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(7))
				.andExpect(jsonPath("$.quantity").value(2))
				.andExpect(jsonPath("$.food.name").value("Burger"));
	}

	@Test
	void getOrderItemById_returns404_whenMissing() throws Exception {
		when(orderItemRepository.findById(99L)).thenReturn(Optional.empty());

		mockMvc.perform(get("/api/v1/order-items/99")).andExpect(status().isNotFound());
	}

	@Test
	void createOrderItem_returns200_andBody() throws Exception {
		Order parentOrder = order(1L, "PENDING");
		Food burger = food(1L, "Burger", 9.99);
		when(orderRepository.findById(1L)).thenReturn(Optional.of(parentOrder));
		when(foodRepository.findById(1L)).thenReturn(Optional.of(burger));

		OrderItem saved = orderItem(10L, parentOrder, burger, 2, 9.99);
		when(orderItemRepository.save(any(OrderItem.class))).thenReturn(saved);
		when(orderItemRepository.findByOrderId(anyLong())).thenReturn(List.of(saved));
		when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

		mockMvc.perform(post("/api/v1/order-items")
				.contentType(MediaType.APPLICATION_JSON)
				.content(
						"{\"order\":{\"id\":1},\"food\":{\"id\":1,\"name\":\"Burger\",\"description\":\"d\",\"price\":9.99,\"imageUrl\":\"i.jpg\"},\"quantity\":2,\"price\":9.99}"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(10))
				.andExpect(jsonPath("$.quantity").value(2))
				.andExpect(jsonPath("$.food.id").value(1));
	}

	@Test
	void createOrderItem_returns404_whenOrderMissing() throws Exception {
		when(orderRepository.findById(42L)).thenReturn(Optional.empty());

		mockMvc.perform(post("/api/v1/order-items")
				.contentType(MediaType.APPLICATION_JSON)
				.content(
						"{\"order\":{\"id\":42},\"food\":{\"id\":1,\"name\":\"Burger\",\"description\":\"d\",\"price\":9.99,\"imageUrl\":\"i.jpg\"},\"quantity\":1,\"price\":9.99}"))
				.andExpect(status().isNotFound());
	}

	@Test
	void createOrderItem_returns404_whenFoodMissing() throws Exception {
		Order parentOrder = order(1L, "PENDING");
		when(orderRepository.findById(1L)).thenReturn(Optional.of(parentOrder));
		when(foodRepository.findById(99L)).thenReturn(Optional.empty());

		mockMvc.perform(post("/api/v1/order-items")
				.contentType(MediaType.APPLICATION_JSON)
				.content(
						"{\"order\":{\"id\":1},\"food\":{\"id\":99,\"name\":\"X\",\"description\":\"d\",\"price\":1,\"imageUrl\":\"x.jpg\"},\"quantity\":1,\"price\":1}"))
				.andExpect(status().isNotFound());
	}

	@Test
	void createOrderItem_returns404_whenOrderIdInvalid() throws Exception {
		mockMvc.perform(post("/api/v1/order-items")
				.contentType(MediaType.APPLICATION_JSON)
				.content(
						"{\"order\":{\"id\":0},\"food\":{\"id\":1,\"name\":\"Burger\",\"description\":\"d\",\"price\":9.99,\"imageUrl\":\"i.jpg\"},\"quantity\":1,\"price\":9.99}"))
				.andExpect(status().isNotFound());
	}

	@Test
	void createOrderItem_returns404_whenFoodIdInvalid() throws Exception {
		Order parentOrder = order(1L, "PENDING");
		when(orderRepository.findById(1L)).thenReturn(Optional.of(parentOrder));

		mockMvc.perform(post("/api/v1/order-items")
				.contentType(MediaType.APPLICATION_JSON)
				.content(
						"{\"order\":{\"id\":1},\"food\":{\"id\":0,\"name\":\"X\",\"description\":\"d\",\"price\":1,\"imageUrl\":\"x.jpg\"},\"quantity\":1,\"price\":1}"))
				.andExpect(status().isNotFound());
	}

	@Test
	void updateOrderItem_returns200_whenFound() throws Exception {
		Order parentOrder = order(1L, "PENDING");
		Food burger = food(1L, "Burger", 9.99);
		OrderItem existing = orderItem(8L, parentOrder, burger, 1, 9.99);
		when(orderItemRepository.findById(8L)).thenReturn(Optional.of(existing));

		OrderItem updated = orderItem(8L, parentOrder, burger, 3, 9.99);
		when(orderItemRepository.save(any(OrderItem.class))).thenReturn(updated);
		when(orderItemRepository.findByOrderId(anyLong())).thenReturn(List.of(updated));
		when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

		mockMvc.perform(put("/api/v1/order-items/8")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"quantity\":3}"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(8))
				.andExpect(jsonPath("$.quantity").value(3));
	}

	@Test
	void updateOrderItem_returns404_whenMissing() throws Exception {
		when(orderItemRepository.findById(8L)).thenReturn(Optional.empty());

		mockMvc.perform(put("/api/v1/order-items/8")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"quantity\":3}"))
				.andExpect(status().isNotFound());
	}

	@Test
	void deleteOrderItem_returns200_andDeletedFlag() throws Exception {
		Order parentOrder = order(1L, "PENDING");
		Food burger = food(1L, "Burger", 9.99);
		OrderItem existing = orderItem(9L, parentOrder, burger, 1, 9.99);
		when(orderItemRepository.findById(9L)).thenReturn(Optional.of(existing));
		when(orderItemRepository.findByOrderId(1L)).thenReturn(Collections.emptyList());
		when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

		mockMvc.perform(delete("/api/v1/order-items/9"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.deleted").value(true));
	}

	@Test
	void deleteOrderItem_returns404_whenMissing() throws Exception {
		when(orderItemRepository.findById(9L)).thenReturn(Optional.empty());

		mockMvc.perform(delete("/api/v1/order-items/9")).andExpect(status().isNotFound());
	}

	private static Food food(long id, String name, double price) {
		Food f = new Food();
		f.setId(id);
		f.setName(name);
		f.setDescription("desc");
		f.setPrice(price);
		f.setImageUrl("img.jpg");
		return f;
	}

	private static Order order(long id, String status) {
		Order o = new Order();
		o.setId(id);
		o.setStatus(status);
		return o;
	}

	private static OrderItem orderItem(long id, Order order, Food food, int qty, double price) {
		OrderItem oi = new OrderItem();
		oi.setId(id);
		oi.setOrder(order);
		oi.setFood(food);
		oi.setQuantity(qty);
		oi.setPrice(price);
		return oi;
	}
}
