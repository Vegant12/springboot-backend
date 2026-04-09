package net.javaguides.springboot.service;

import java.util.List;

import org.springframework.stereotype.Service;

import net.javaguides.springboot.exception.ResourceNotFoundException;
import net.javaguides.springboot.model.Food;
import net.javaguides.springboot.model.Order;
import net.javaguides.springboot.model.OrderItem;
import net.javaguides.springboot.repository.FoodRepository;
import net.javaguides.springboot.repository.OrderItemRepository;
import net.javaguides.springboot.repository.OrderRepository;

@Service
public class OrderItemService {

    private final OrderItemRepository orderItemRepository;
    private final OrderRepository orderRepository;
    private final FoodRepository foodRepository;

    public OrderItemService(
            OrderItemRepository orderItemRepository,
            OrderRepository orderRepository,
            FoodRepository foodRepository) {
        this.orderItemRepository = orderItemRepository;
        this.orderRepository = orderRepository;
        this.foodRepository = foodRepository;
    }

    public List<OrderItem> getOrderItems(Long orderId) {
        if (orderId != null) {
            return orderItemRepository.findByOrderId(orderId);
        }
        return orderItemRepository.findAll();
    }

    public OrderItem getOrderItemById(Long id) {
        return orderItemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order item not found with id: " + id));
    }

    public OrderItem createOrderItem(OrderItem itemRequest) {
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

    public OrderItem updateOrderItem(Long id, OrderItem itemRequest) {
        OrderItem orderItem = getOrderItemById(id);

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

    public void deleteOrderItem(Long id) {
        OrderItem orderItem = getOrderItemById(id);
        Order order = orderItem.getOrder();
        orderItemRepository.delete(orderItem);
        recalculateOrderTotal(order);
    }

    private void recalculateOrderTotal(Order order) {
        List<OrderItem> items = orderItemRepository.findByOrderId(order.getId());
        double total = items.stream().mapToDouble(item -> item.getPrice() * item.getQuantity()).sum();
        order.setTotalAmount(total);
        orderRepository.save(order);
    }
}
