package net.javaguides.springboot.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import net.javaguides.springboot.exception.ResourceNotFoundException;
import net.javaguides.springboot.model.Food;
import net.javaguides.springboot.model.Order;
import net.javaguides.springboot.model.OrderItem;
import net.javaguides.springboot.repository.FoodRepository;
import net.javaguides.springboot.repository.OrderRepository;

@RestController
@RequestMapping("/api/v1/")
public class OrderController {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private FoodRepository foodRepository;

    @GetMapping("/orders")
    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    @GetMapping("/orders/{id}")
    public Order getOrderById(@PathVariable Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + id));
    }

    @PostMapping("/orders")
    public Order createOrder(@RequestBody Order orderRequest) {
        Order order = new Order();
        if (orderRequest.getStatus() != null && !orderRequest.getStatus().isBlank()) {
            order.setStatus(orderRequest.getStatus());
        }

        List<OrderItem> normalizedItems = buildOrderItems(order, orderRequest.getItems());
        order.setItems(normalizedItems);
        order.setTotalAmount(calculateTotal(normalizedItems));

        return orderRepository.save(order);
    }

    @PutMapping("/orders/{id}")
    public Order updateOrder(@PathVariable Long id, @RequestBody Order orderRequest) {
        Order existingOrder = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + id));

        if (orderRequest.getStatus() != null && !orderRequest.getStatus().isBlank()) {
            existingOrder.setStatus(orderRequest.getStatus());
        }

        if (orderRequest.getItems() != null) {
            List<OrderItem> normalizedItems = buildOrderItems(existingOrder, orderRequest.getItems());
            existingOrder.getItems().clear();
            existingOrder.getItems().addAll(normalizedItems);
            existingOrder.setTotalAmount(calculateTotal(existingOrder.getItems()));
        }

        return orderRepository.save(existingOrder);
    }

    @DeleteMapping("/orders/{id}")
    public Map<String, Boolean> deleteOrder(@PathVariable Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + id));
        orderRepository.delete(order);
        return Map.of("deleted", Boolean.TRUE);
    }

    private List<OrderItem> buildOrderItems(Order order, List<OrderItem> incomingItems) {
        List<OrderItem> items = new ArrayList<>();
        if (incomingItems == null) {
            return items;
        }

        for (OrderItem incoming : incomingItems) {
            if (incoming.getFood() == null || incoming.getFood().getId() <= 0) {
                throw new ResourceNotFoundException("Valid food id is required for each order item");
            }

            Food food = foodRepository.findById(incoming.getFood().getId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Food not found with id: " + incoming.getFood().getId()));

            OrderItem item = new OrderItem();
            item.setOrder(order);
            item.setFood(food);
            item.setQuantity(incoming.getQuantity() > 0 ? incoming.getQuantity() : 1);
            item.setPrice(incoming.getPrice() > 0 ? incoming.getPrice() : food.getPrice());
            items.add(item);
        }

        return items;
    }

    private double calculateTotal(List<OrderItem> items) {
        return items.stream().mapToDouble(item -> item.getPrice() * item.getQuantity()).sum();
    }
}
