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
import org.springframework.web.bind.annotation.RestController;

import net.javaguides.springboot.exception.ResourceNotFoundException;
import net.javaguides.springboot.model.Cart;
import net.javaguides.springboot.model.Food;
import net.javaguides.springboot.repository.CartRepository;
import net.javaguides.springboot.repository.FoodRepository;

@RestController
@RequestMapping("/api/v1/")
public class CartController {

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private FoodRepository foodRepository;

    @GetMapping("/carts")
    public List<Cart> getAllCartItems() {
        return cartRepository.findAll();
    }

    @GetMapping("/carts/{id}")
    public Cart getCartItemById(@PathVariable Long id) {
        return cartRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found with id: " + id));
    }

    @PostMapping("/carts")
    public Cart createCartItem(@RequestBody Cart cartRequest) {
        if (cartRequest.getFood() == null || cartRequest.getFood().getId() <= 0) {
            throw new ResourceNotFoundException("Valid food id is required");
        }

        Food food = foodRepository.findById(cartRequest.getFood().getId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Food not found with id: " + cartRequest.getFood().getId()));

        Cart cartItem = new Cart();
        cartItem.setFood(food);
        cartItem.setQuantity(cartRequest.getQuantity() > 0 ? cartRequest.getQuantity() : 1);

        return cartRepository.save(cartItem);
    }

    @PutMapping("/carts/{id}")
    public Cart updateCartItem(@PathVariable Long id, @RequestBody Cart cartRequest) {
        Cart cartItem = cartRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found with id: " + id));

        if (cartRequest.getFood() != null && cartRequest.getFood().getId() > 0) {
            Food food = foodRepository.findById(cartRequest.getFood().getId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Food not found with id: " + cartRequest.getFood().getId()));
            cartItem.setFood(food);
        }

        if (cartRequest.getQuantity() > 0) {
            cartItem.setQuantity(cartRequest.getQuantity());
        }

        return cartRepository.save(cartItem);
    }

    @DeleteMapping("/carts/{id}")
    public Map<String, Boolean> deleteCartItem(@PathVariable Long id) {
        Cart cartItem = cartRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found with id: " + id));
        cartRepository.delete(cartItem);
        return Map.of("deleted", Boolean.TRUE);
    }
}
