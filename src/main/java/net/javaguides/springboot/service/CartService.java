package net.javaguides.springboot.service;

import java.util.List;

import org.springframework.stereotype.Service;

import net.javaguides.springboot.exception.ResourceNotFoundException;
import net.javaguides.springboot.model.Cart;
import net.javaguides.springboot.model.Food;
import net.javaguides.springboot.repository.CartRepository;
import net.javaguides.springboot.repository.FoodRepository;

@Service
public class CartService {

    private final CartRepository cartRepository;
    private final FoodRepository foodRepository;

    public CartService(CartRepository cartRepository, FoodRepository foodRepository) {
        this.cartRepository = cartRepository;
        this.foodRepository = foodRepository;
    }

    public List<Cart> getAllCartItems() {
        return cartRepository.findAll();
    }

    public Cart getCartItemById(Long id) {
        return cartRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found with id: " + id));
    }

    public Cart createCartItem(Cart cartRequest) {
        if (cartRequest.getFood() == null || cartRequest.getFood().getId() <= 0) {
            throw new ResourceNotFoundException("Valid food id is required");
        }

        Long foodId = cartRequest.getFood().getId();
        Food food = foodRepository.findById(foodId).orElseThrow(() -> new ResourceNotFoundException("Food not found with id: " + foodId));

        int requestedQuantity = cartRequest.getQuantity() > 0 ? cartRequest.getQuantity() : 1;

        Cart existingItem = cartRepository.findByFoodId(foodId).orElse(null);
        if (existingItem != null) {
            existingItem.setQuantity(existingItem.getQuantity() + requestedQuantity);
            return cartRepository.save(existingItem);
        }

        Cart cartItem = new Cart();
        cartItem.setFood(food);
        cartItem.setQuantity(requestedQuantity);
        return cartRepository.save(cartItem);
    }

    public Cart updateCartItem(Long id, Cart cartRequest) {
        Cart cartItem = getCartItemById(id);

        if (cartRequest.getFood() != null && cartRequest.getFood().getId() > 0) {
            Food food = foodRepository.findById(cartRequest.getFood().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Food not found with id: " + cartRequest.getFood().getId()));
            cartItem.setFood(food);
        }

        if (cartRequest.getQuantity() > 0) {
            cartItem.setQuantity(cartRequest.getQuantity());
        }

        return cartRepository.save(cartItem);
    }

    public void deleteCartItem(Long id) {
        Cart cartItem = getCartItemById(id);
        cartRepository.delete(cartItem);
    }
}
