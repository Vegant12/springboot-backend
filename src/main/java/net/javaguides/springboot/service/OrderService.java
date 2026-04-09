package net.javaguides.springboot.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import net.javaguides.springboot.exception.ResourceNotFoundException;
import net.javaguides.springboot.model.Food;
import net.javaguides.springboot.model.Order;
import net.javaguides.springboot.model.OrderItem;
import net.javaguides.springboot.repository.FoodRepository;
import net.javaguides.springboot.repository.OrderRepository;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final FoodRepository foodRepository;

    public OrderService(OrderRepository orderRepository, FoodRepository foodRepository) {
        this.orderRepository = orderRepository;
        this.foodRepository = foodRepository;
    }

    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    public Order getOrderById(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + id));
    }

    public Order createOrder(Order orderRequest) {
        Order order = new Order();
        if (orderRequest.getStatus() != null && !orderRequest.getStatus().isBlank()) {
            order.setStatus(orderRequest.getStatus());
        }

        List<OrderItem> normalizedItems = buildOrderItems(order, orderRequest.getItems());
        order.setItems(normalizedItems);
        order.setTotalAmount(calculateTotal(normalizedItems));

        return orderRepository.save(order);
    }

    public Order updateOrder(Long id, Order orderRequest) {
        Order existingOrder = getOrderById(id);

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

    public void deleteOrder(Long id) {
        Order order = getOrderById(id);
        orderRepository.delete(order);
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
