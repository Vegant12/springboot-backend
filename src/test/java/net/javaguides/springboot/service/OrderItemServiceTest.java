package net.javaguides.springboot.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import net.javaguides.springboot.exception.ResourceNotFoundException;
import net.javaguides.springboot.model.Food;
import net.javaguides.springboot.model.Order;
import net.javaguides.springboot.model.OrderItem;
import net.javaguides.springboot.repository.FoodRepository;
import net.javaguides.springboot.repository.OrderItemRepository;
import net.javaguides.springboot.repository.OrderRepository;

@ExtendWith(MockitoExtension.class)
class OrderItemServiceTest {

    @Mock
    private OrderItemRepository orderItemRepository;
    @Mock
    private OrderRepository orderRepository;
    @Mock
    private FoodRepository foodRepository;

    @InjectMocks
    private OrderItemService orderItemService;

    @Test
    void getOrderItems_filtersWhenOrderIdProvided() {
        when(orderItemRepository.findByOrderId(3L)).thenReturn(List.of(new OrderItem()));
        assertEquals(1, orderItemService.getOrderItems(3L).size());
    }

    @Test
    void createOrderItem_defaultsPriceFromFood() {
        Order order = new Order();
        order.setId(1L);
        Food food = new Food();
        food.setId(1L);
        food.setPrice(7.5);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(foodRepository.findById(1L)).thenReturn(Optional.of(food));
        when(orderItemRepository.save(org.mockito.ArgumentMatchers.any(OrderItem.class)))
                .thenAnswer(inv -> inv.getArgument(0));
        when(orderItemRepository.findByOrderId(1L)).thenReturn(Collections.emptyList());
        when(orderRepository.save(org.mockito.ArgumentMatchers.any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        OrderItem request = new OrderItem();
        Order orderRef = new Order();
        orderRef.setId(1L);
        Food foodRef = new Food();
        foodRef.setId(1L);
        request.setOrder(orderRef);
        request.setFood(foodRef);
        request.setQuantity(1);
        request.setPrice(0);

        OrderItem result = orderItemService.createOrderItem(request);
        assertEquals(7.5, result.getPrice());
    }

    @Test
    void updateOrderItem_throwsWhenMissing() {
        when(orderItemRepository.findById(10L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> orderItemService.updateOrderItem(10L, new OrderItem()));
    }

    @Test
    void deleteOrderItem_deletesAndRecalculates() {
        Order order = new Order();
        order.setId(5L);
        OrderItem item = new OrderItem();
        item.setId(2L);
        item.setOrder(order);
        item.setPrice(3);
        item.setQuantity(2);
        when(orderItemRepository.findById(2L)).thenReturn(Optional.of(item));
        when(orderItemRepository.findByOrderId(5L)).thenReturn(Collections.emptyList());
        when(orderRepository.save(org.mockito.ArgumentMatchers.any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        orderItemService.deleteOrderItem(2L);
        verify(orderItemRepository).delete(item);
    }
}
