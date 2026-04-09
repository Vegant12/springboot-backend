package net.javaguides.springboot.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
import net.javaguides.springboot.repository.OrderRepository;

@WebMvcTest(OrderController.class)
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private OrderRepository orderRepository;

    @MockitoBean
    private FoodRepository foodRepository;

    @Test
    void getOrderById_returns200_whenFound() throws Exception {
        Food food = food(1L, "Burger", 10.0);
        Order order = orderWithItem(5L, "SHIPPED", food, 2, 10.0);

        when(orderRepository.findById(5L)).thenReturn(Optional.of(order));

        mockMvc.perform(get("/api/v1/orders/5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5))
                .andExpect(jsonPath("$.status").value("SHIPPED"))
                .andExpect(jsonPath("$.totalAmount").value(20.0))
                .andExpect(jsonPath("$.items[0].quantity").value(2))
                .andExpect(jsonPath("$.items[0].food.id").value(1))
                .andExpect(jsonPath("$.items[0].food.name").value("Burger"));
    }

    @Test
    void getOrderById_returns404_whenMissing() throws Exception {
        when(orderRepository.findById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/orders/99")).andExpect(status().isNotFound());
    }

    @Test
    void getAllOrders_returns200() throws Exception {
        Food food = food(1L, "Burger", 10.0);
        when(orderRepository.findAll()).thenReturn(List.of(orderWithItem(1L, "PENDING", food, 1, 10.0)));

        mockMvc.perform(get("/api/v1/orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].status").value("PENDING"));
    }

    @Test
    void createOrder_returns200_andBody() throws Exception {
        Food food = food(1L, "Burger", 9.99);
        when(foodRepository.findById(1L)).thenReturn(Optional.of(food));

        Order saved = orderWithItem(10L, "PENDING", food, 2, 9.99);
        when(orderRepository.save(any(Order.class))).thenReturn(saved);

        mockMvc.perform(post("/api/v1/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                        "{\"status\":\"PENDING\",\"items\":[{\"food\":{\"id\":1,\"name\":\"Burger\",\"description\":\"d\",\"price\":9.99,\"imageUrl\":\"i.jpg\"},\"quantity\":2,\"price\":9.99}]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.items[0].quantity").value(2));
    }

    @Test
    void createOrder_returns404_whenFoodMissing() throws Exception {
        when(foodRepository.findById(42L)).thenReturn(Optional.empty());

        mockMvc.perform(post("/api/v1/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                        "{\"status\":\"PENDING\",\"items\":[{\"food\":{\"id\":42,\"name\":\"X\",\"description\":\"d\",\"price\":1,\"imageUrl\":\"x.jpg\"},\"quantity\":1,\"price\":1}]}"))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateOrder_returns200_whenFound() throws Exception {
        Food food = food(1L, "Burger", 9.99);
        Order existing = orderWithItem(7L, "PENDING", food, 1, 9.99);
        when(orderRepository.findById(7L)).thenReturn(Optional.of(existing));

        Order updated = orderWithItem(7L, "COMPLETED", food, 1, 9.99);
        when(orderRepository.save(any(Order.class))).thenReturn(updated);

        mockMvc.perform(put("/api/v1/orders/7")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"status\":\"COMPLETED\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"));
    }

    @Test
    void updateOrder_returns404_whenMissing() throws Exception {
        when(orderRepository.findById(7L)).thenReturn(Optional.empty());

        mockMvc.perform(put("/api/v1/orders/7")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"status\":\"COMPLETED\"}"))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteOrder_returns200_andDeletedFlag() throws Exception {
        Food food = food(1L, "Burger", 9.99);
        Order existing = orderWithItem(8L, "PENDING", food, 1, 9.99);
        when(orderRepository.findById(8L)).thenReturn(Optional.of(existing));

        mockMvc.perform(delete("/api/v1/orders/8"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.deleted").value(true));
    }

    @Test
    void deleteOrder_returns404_whenMissing() throws Exception {
        when(orderRepository.findById(8L)).thenReturn(Optional.empty());

        mockMvc.perform(delete("/api/v1/orders/8")).andExpect(status().isNotFound());
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

    private static Order orderWithItem(long id, String status, Food food, int quantity, double linePrice) {
        Order order = new Order();
        order.setId(id);
        order.setStatus(status);

        OrderItem item = new OrderItem();
        item.setId(1L);
        item.setOrder(order);
        item.setFood(food);
        item.setQuantity(quantity);
        item.setPrice(linePrice);

        order.setItems(new java.util.ArrayList<>(List.of(item)));
        order.setTotalAmount(linePrice * quantity);
        return order;
    }
}

