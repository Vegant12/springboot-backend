package net.javaguides.springboot.controller;

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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import net.javaguides.springboot.exception.ResourceNotFoundException;
import net.javaguides.springboot.model.Food;
import net.javaguides.springboot.model.Order;
import net.javaguides.springboot.model.OrderItem;
import net.javaguides.springboot.repository.FoodRepository;
import net.javaguides.springboot.repository.OrderItemRepository;
import net.javaguides.springboot.repository.OrderRepository;

@RestController
@RequestMapping("/api/v1/")
public class OrderItemController {

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private FoodRepository foodRepository;

    @GetMapping("/order-items")
    public List<OrderItem> getOrderItems(@RequestParam(required = false) Long orderId) {
        if (orderId != null) {
            return orderItemRepository.findByOrderId(orderId);
        }
        return orderItemRepository.findAll();
    }

    @GetMapping("/order-items/{id}")
    public OrderItem getOrderItemById(@PathVariable Long id) {
        return orderItemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order item not found with id: " + id));
    }

    @PostMapping("/order-items")
    public OrderItem createOrderItem(@RequestBody OrderItem itemRequest) {
        if (itemRequest.getOrder() == null || itemRequest.getOrder().getId() <= 0) {
            throw new ResourceNotFoundException("Valid order id is required");
        }
        if (itemRequest.getFood() == null || itemRequest.getFood().getId() <= 0) {
            throw new ResourceNotFoundException("Valid food id is required");
        }

        Order order = orderRepository.findById(itemRequest.getOrder().getId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Order not found with id: " + itemRequest.getOrder().getId()));
        Food food = foodRepository.findById(itemRequest.getFood().getId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Food not found with id: " + itemRequest.getFood().getId()));

        OrderItem orderItem = new OrderItem();
        orderItem.setOrder(order);
        orderItem.setFood(food);
        orderItem.setQuantity(itemRequest.getQuantity() > 0 ? itemRequest.getQuantity() : 1);
        orderItem.setPrice(itemRequest.getPrice() > 0 ? itemRequest.getPrice() : food.getPrice());

        OrderItem savedItem = orderItemRepository.save(orderItem);
        recalculateOrderTotal(order);
        return savedItem;
    }

    @PutMapping("/order-items/{id}")
    public OrderItem updateOrderItem(@PathVariable Long id, @RequestBody OrderItem itemRequest) {
        OrderItem orderItem = orderItemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order item not found with id: " + id));

        if (itemRequest.getOrder() != null && itemRequest.getOrder().getId() > 0) {
            Order order = orderRepository.findById(itemRequest.getOrder().getId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Order not found with id: " + itemRequest.getOrder().getId()));
            orderItem.setOrder(order);
        }

        if (itemRequest.getFood() != null && itemRequest.getFood().getId() > 0) {
            Food food = foodRepository.findById(itemRequest.getFood().getId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Food not found with id: " + itemRequest.getFood().getId()));
            orderItem.setFood(food);
        }

        if (itemRequest.getQuantity() > 0) {
            orderItem.setQuantity(itemRequest.getQuantity());
        }

        if (itemRequest.getPrice() > 0) {
            orderItem.setPrice(itemRequest.getPrice());
        } else if (orderItem.getFood() != null) {
            orderItem.setPrice(orderItem.getFood().getPrice());
        }

        OrderItem savedItem = orderItemRepository.save(orderItem);
        recalculateOrderTotal(savedItem.getOrder());
        return savedItem;
    }

    @DeleteMapping("/order-items/{id}")
    public Map<String, Boolean> deleteOrderItem(@PathVariable Long id) {
        OrderItem orderItem = orderItemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order item not found with id: " + id));
        Order order = orderItem.getOrder();
        orderItemRepository.delete(orderItem);
        recalculateOrderTotal(order);
        return Map.of("deleted", Boolean.TRUE);
    }

    private void recalculateOrderTotal(Order order) {
        List<OrderItem> items = orderItemRepository.findByOrderId(order.getId());
        double total = items.stream().mapToDouble(item -> item.getPrice() * item.getQuantity()).sum();
        order.setTotalAmount(total);
        orderRepository.save(order);
    }
}
