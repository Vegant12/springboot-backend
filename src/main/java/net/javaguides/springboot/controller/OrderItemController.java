package net.javaguides.springboot.controller;

import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import net.javaguides.springboot.model.OrderItem;
import net.javaguides.springboot.service.OrderItemService;

@RestController
@RequestMapping("/api/v1/")
public class OrderItemController {

    private final OrderItemService orderItemService;

    public OrderItemController(OrderItemService orderItemService) {
        this.orderItemService = orderItemService;
    }

    @GetMapping("/order-items")
    public List<OrderItem> getOrderItems(@RequestParam(required = false) Long orderId) {
        return orderItemService.getOrderItems(orderId);
    }

    @GetMapping("/order-items/{id}")
    public OrderItem getOrderItemById(@PathVariable Long id) {
        return orderItemService.getOrderItemById(id);
    }

    @PostMapping("/order-items")
    public OrderItem createOrderItem(@RequestBody OrderItem itemRequest) {
        return orderItemService.createOrderItem(itemRequest);
    }

    @PutMapping("/order-items/{id}")
    public OrderItem updateOrderItem(@PathVariable Long id, @RequestBody OrderItem itemRequest) {
        return orderItemService.updateOrderItem(id, itemRequest);
    }

    @DeleteMapping("/order-items/{id}")
    public Map<String, Boolean> deleteOrderItem(@PathVariable Long id) {
        orderItemService.deleteOrderItem(id);
        return Map.of("deleted", Boolean.TRUE);
    }
}
