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
import org.springframework.web.bind.annotation.RestController;

import net.javaguides.springboot.model.Cart;
import net.javaguides.springboot.service.CartService;

@RestController
@RequestMapping("/api/v1/")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @GetMapping("/carts")
    public List<Cart> getAllCartItems() {
        return cartService.getAllCartItems();
    }

    @GetMapping("/carts/{id}")
    public Cart getCartItemById(@PathVariable Long id) {
        return cartService.getCartItemById(id);
    }

    @PostMapping("/carts")
    public Cart createCartItem(@RequestBody Cart cartRequest) {
        return cartService.createCartItem(cartRequest);
    }

    @PutMapping("/carts/{id}")
    public Cart updateCartItem(@PathVariable Long id, @RequestBody Cart cartRequest) {
        return cartService.updateCartItem(id, cartRequest);
    }

    @DeleteMapping("/carts/{id}")
    public Map<String, Boolean> deleteCartItem(@PathVariable Long id) {
        cartService.deleteCartItem(id);
        return Map.of("deleted", Boolean.TRUE);
    }
}
