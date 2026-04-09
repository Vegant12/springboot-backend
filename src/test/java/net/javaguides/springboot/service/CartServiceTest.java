package net.javaguides.springboot.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import net.javaguides.springboot.exception.ResourceNotFoundException;
import net.javaguides.springboot.model.Cart;
import net.javaguides.springboot.model.Food;
import net.javaguides.springboot.repository.CartRepository;
import net.javaguides.springboot.repository.FoodRepository;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @Mock
    private CartRepository cartRepository;
    @Mock
    private FoodRepository foodRepository;

    @InjectMocks
    private CartService cartService;

    @Test
    void createCartItem_defaultsQuantityToOne() {
        Food food = new Food();
        food.setId(1L);
        when(foodRepository.findById(1L)).thenReturn(Optional.of(food));

        Cart request = new Cart();
        request.setFood(food);
        request.setQuantity(0);

        Cart saved = new Cart();
        saved.setFood(food);
        saved.setQuantity(1);
        when(cartRepository.save(org.mockito.ArgumentMatchers.any(Cart.class))).thenReturn(saved);

        Cart result = cartService.createCartItem(request);
        assertEquals(1, result.getQuantity());
    }

    @Test
    void createCartItem_throws_whenFoodIdInvalid() {
        Cart request = new Cart();
        request.setFood(new Food());
        assertThrows(ResourceNotFoundException.class, () -> cartService.createCartItem(request));
    }

    @Test
    void updateCartItem_updatesQuantity() {
        Food food = new Food();
        food.setId(1L);
        Cart existing = new Cart();
        existing.setId(5L);
        existing.setFood(food);
        existing.setQuantity(1);
        when(cartRepository.findById(5L)).thenReturn(Optional.of(existing));
        when(cartRepository.save(existing)).thenReturn(existing);

        Cart request = new Cart();
        request.setQuantity(3);

        Cart result = cartService.updateCartItem(5L, request);
        assertEquals(3, result.getQuantity());
    }

    @Test
    void deleteCartItem_deletesResolvedEntity() {
        Cart existing = new Cart();
        existing.setId(7L);
        when(cartRepository.findById(7L)).thenReturn(Optional.of(existing));

        cartService.deleteCartItem(7L);
        verify(cartRepository).delete(existing);
    }
}
