package net.javaguides.springboot.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
import net.javaguides.springboot.repository.OrderRepository;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;
    @Mock
    private FoodRepository foodRepository;

    @InjectMocks
    private OrderService orderService;

    @Test
    void createOrder_calculatesTotal() {
        Food food = new Food();
        food.setId(1L);
        food.setPrice(10.0);
        when(foodRepository.findById(1L)).thenReturn(Optional.of(food));

        Order request = new Order();
        OrderItem incoming = new OrderItem();
        Food ref = new Food();
        ref.setId(1L);
        incoming.setFood(ref);
        incoming.setQuantity(2);
        incoming.setPrice(10.0);
        request.setItems(List.of(incoming));

        when(orderRepository.save(org.mockito.ArgumentMatchers.any(Order.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        Order result = orderService.createOrder(request);
        assertEquals(20.0, result.getTotalAmount());
    }

    @Test
    void createOrder_throws_whenFoodMissing() {
        Order request = new Order();
        OrderItem incoming = new OrderItem();
        Food ref = new Food();
        ref.setId(99L);
        incoming.setFood(ref);
        request.setItems(List.of(incoming));
        when(foodRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> orderService.createOrder(request));
    }

    @Test
    void updateOrder_throws_whenOrderMissing() {
        when(orderRepository.findById(8L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> orderService.updateOrder(8L, new Order()));
    }

    @Test
    void deleteOrder_deletesResolvedEntity() {
        Order existing = new Order();
        existing.setId(4L);
        when(orderRepository.findById(4L)).thenReturn(Optional.of(existing));

        orderService.deleteOrder(4L);
        verify(orderRepository).delete(existing);
    }
}
