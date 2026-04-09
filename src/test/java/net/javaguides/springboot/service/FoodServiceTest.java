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
import net.javaguides.springboot.model.Food;
import net.javaguides.springboot.repository.FoodRepository;

@ExtendWith(MockitoExtension.class)
class FoodServiceTest {

    @Mock
    private FoodRepository foodRepository;

    @InjectMocks
    private FoodService foodService;

    @Test
    void getFoodById_returnsFood_whenFound() {
        Food food = new Food();
        food.setId(1L);
        when(foodRepository.findById(1L)).thenReturn(Optional.of(food));

        Food result = foodService.getFoodById(1L);
        assertEquals(1L, result.getId());
    }

    @Test
    void getFoodById_throws_whenMissing() {
        when(foodRepository.findById(9L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> foodService.getFoodById(9L));
    }

    @Test
    void updateFood_updatesFields() {
        Food existing = new Food();
        existing.setId(2L);
        existing.setName("Old");
        when(foodRepository.findById(2L)).thenReturn(Optional.of(existing));
        when(foodRepository.save(existing)).thenReturn(existing);

        Food incoming = new Food();
        incoming.setName("New");
        incoming.setDescription("Desc");
        incoming.setPrice(11.5);
        incoming.setImageUrl("img.jpg");

        Food updated = foodService.updateFood(2L, incoming);
        assertEquals("New", updated.getName());
        assertEquals(11.5, updated.getPrice());
    }

    @Test
    void deleteFood_deletesResolvedEntity() {
        Food existing = new Food();
        existing.setId(3L);
        when(foodRepository.findById(3L)).thenReturn(Optional.of(existing));

        foodService.deleteFood(3L);
        verify(foodRepository).delete(existing);
    }
}
