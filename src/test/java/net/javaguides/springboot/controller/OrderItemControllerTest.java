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

import net.javaguides.springboot.model.Food;
import net.javaguides.springboot.model.Order;
import net.javaguides.springboot.model.OrderItem;
import net.javaguides.springboot.service.OrderItemService;

@WebMvcTest(OrderItemController.class)
class OrderItemControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private OrderItemService orderItemService;

	@Test
	void getOrderItems_returnsAll_whenNoOrderId() throws Exception {
		Order parentOrder = order(1L, "PENDING");
		Food burger = food(1L, "Burger", 10.0);
		OrderItem item = orderItem(1L, parentOrder, burger, 2, 10.0);

		when(orderItemService.getOrderItems(null)).thenReturn(List.of(item));

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

		when(orderItemService.getOrderItems(5L)).thenReturn(List.of(item));

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

		when(orderItemService.getOrderItemById(7L)).thenReturn(item);

		mockMvc.perform(get("/api/v1/order-items/7"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(7))
				.andExpect(jsonPath("$.quantity").value(2))
				.andExpect(jsonPath("$.food.name").value("Burger"));
	}

	@Test
	void getOrderItemById_returns404_whenMissing() throws Exception {
		when(orderItemService.getOrderItemById(99L))
				.thenThrow(new net.javaguides.springboot.exception.ResourceNotFoundException("Order item not found with id: 99"));

		mockMvc.perform(get("/api/v1/order-items/99")).andExpect(status().isNotFound());
	}

	@Test
	void createOrderItem_returns200_andBody() throws Exception {
		Order parentOrder = order(1L, "PENDING");
		Food burger = food(1L, "Burger", 9.99);
		OrderItem saved = orderItem(10L, parentOrder, burger, 2, 9.99);
		when(orderItemService.createOrderItem(any(OrderItem.class))).thenReturn(saved);

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
		when(orderItemService.createOrderItem(any(OrderItem.class)))
				.thenThrow(new net.javaguides.springboot.exception.ResourceNotFoundException("Order not found with id: 42"));

		mockMvc.perform(post("/api/v1/order-items")
				.contentType(MediaType.APPLICATION_JSON)
				.content(
						"{\"order\":{\"id\":42},\"food\":{\"id\":1,\"name\":\"Burger\",\"description\":\"d\",\"price\":9.99,\"imageUrl\":\"i.jpg\"},\"quantity\":1,\"price\":9.99}"))
				.andExpect(status().isNotFound());
	}

	@Test
	void createOrderItem_returns404_whenFoodMissing() throws Exception {
		when(orderItemService.createOrderItem(any(OrderItem.class)))
				.thenThrow(new net.javaguides.springboot.exception.ResourceNotFoundException("Food not found with id: 99"));
		mockMvc.perform(post("/api/v1/order-items")
				.contentType(MediaType.APPLICATION_JSON)
				.content(
						"{\"order\":{\"id\":1},\"food\":{\"id\":99,\"name\":\"X\",\"description\":\"d\",\"price\":1,\"imageUrl\":\"x.jpg\"},\"quantity\":1,\"price\":1}"))
				.andExpect(status().isNotFound());
	}

	@Test
	void createOrderItem_returns404_whenOrderIdInvalid() throws Exception {
		when(orderItemService.createOrderItem(any(OrderItem.class)))
				.thenThrow(new net.javaguides.springboot.exception.ResourceNotFoundException("Valid order id is required"));
		mockMvc.perform(post("/api/v1/order-items")
				.contentType(MediaType.APPLICATION_JSON)
				.content(
						"{\"order\":{\"id\":0},\"food\":{\"id\":1,\"name\":\"Burger\",\"description\":\"d\",\"price\":9.99,\"imageUrl\":\"i.jpg\"},\"quantity\":1,\"price\":9.99}"))
				.andExpect(status().isNotFound());
	}

	@Test
	void createOrderItem_returns404_whenFoodIdInvalid() throws Exception {
		when(orderItemService.createOrderItem(any(OrderItem.class)))
				.thenThrow(new net.javaguides.springboot.exception.ResourceNotFoundException("Valid food id is required"));

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
		OrderItem updated = orderItem(8L, parentOrder, burger, 3, 9.99);
		when(orderItemService.updateOrderItem(any(Long.class), any(OrderItem.class))).thenReturn(updated);

		mockMvc.perform(put("/api/v1/order-items/8")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"quantity\":3}"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(8))
				.andExpect(jsonPath("$.quantity").value(3));
	}

	@Test
	void updateOrderItem_returns404_whenMissing() throws Exception {
		when(orderItemService.updateOrderItem(any(Long.class), any(OrderItem.class)))
				.thenThrow(new net.javaguides.springboot.exception.ResourceNotFoundException("Order item not found with id: 8"));

		mockMvc.perform(put("/api/v1/order-items/8")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"quantity\":3}"))
				.andExpect(status().isNotFound());
	}

	@Test
	void deleteOrderItem_returns200_andDeletedFlag() throws Exception {
		doNothing().when(orderItemService).deleteOrderItem(9L);

		mockMvc.perform(delete("/api/v1/order-items/9"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.deleted").value(true));
	}

	@Test
	void deleteOrderItem_returns404_whenMissing() throws Exception {
		doThrow(new net.javaguides.springboot.exception.ResourceNotFoundException("Order item not found with id: 9"))
				.when(orderItemService).deleteOrderItem(9L);

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
