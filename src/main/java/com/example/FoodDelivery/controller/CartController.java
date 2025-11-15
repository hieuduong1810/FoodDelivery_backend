package com.example.FoodDelivery.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.turkraft.springfilter.boot.Filter;

import com.example.FoodDelivery.domain.Cart;
import com.example.FoodDelivery.domain.res.ResultPaginationDTO;
import com.example.FoodDelivery.service.CartService;
import com.example.FoodDelivery.util.annotation.ApiMessage;
import com.example.FoodDelivery.util.error.IdInvalidException;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@RestController
@RequestMapping("/api/v1")
public class CartController {
    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @PostMapping("/carts")
    @ApiMessage("Create cart")
    public ResponseEntity<Cart> createCart(@RequestBody Cart cart) throws IdInvalidException {
        Cart createdCart = cartService.createCart(cart);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdCart);
    }

    @PutMapping("/carts")
    @ApiMessage("Update cart")
    public ResponseEntity<Cart> updateCart(@RequestBody Cart cart) throws IdInvalidException {
        Cart updatedCart = cartService.updateCart(cart);
        return ResponseEntity.ok(updatedCart);
    }

    @GetMapping("/carts")
    @ApiMessage("Get all carts")
    public ResponseEntity<ResultPaginationDTO> getAllCarts(
            @Filter Specification<Cart> spec, Pageable pageable) {
        ResultPaginationDTO result = cartService.getAllCarts(spec, pageable);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/carts/{id}")
    @ApiMessage("Get cart by id")
    public ResponseEntity<Cart> getCartById(@PathVariable("id") Long id) throws IdInvalidException {
        Cart cart = cartService.getCartById(id);
        if (cart == null) {
            throw new IdInvalidException("Cart not found with id: " + id);
        }
        return ResponseEntity.ok(cart);
    }

    @GetMapping("/carts/customer/{customerId}")
    @ApiMessage("Get cart by customer id")
    public ResponseEntity<Cart> getCartByCustomerId(@PathVariable("customerId") Long customerId)
            throws IdInvalidException {
        Cart cart = cartService.getCartByCustomerId(customerId);
        if (cart == null) {
            throw new IdInvalidException("Cart not found for customer id: " + customerId);
        }
        return ResponseEntity.ok(cart);
    }

    @GetMapping("/carts/customer/{customerId}/restaurant/{restaurantId}")
    @ApiMessage("Get cart by customer and restaurant")
    public ResponseEntity<Cart> getCartByCustomerIdAndRestaurantId(
            @PathVariable("customerId") Long customerId,
            @PathVariable("restaurantId") Long restaurantId) throws IdInvalidException {
        Cart cart = cartService.getCartByCustomerIdAndRestaurantId(customerId, restaurantId);
        if (cart == null) {
            throw new IdInvalidException(
                    "Cart not found for customer id: " + customerId + " and restaurant id: " + restaurantId);
        }
        return ResponseEntity.ok(cart);
    }

    @DeleteMapping("/carts/{id}")
    @ApiMessage("Delete cart by id")
    public ResponseEntity<Void> deleteCart(@PathVariable("id") Long id) throws IdInvalidException {
        Cart cart = cartService.getCartById(id);
        if (cart == null) {
            throw new IdInvalidException("Cart not found with id: " + id);
        }
        cartService.deleteCart(id);
        return ResponseEntity.ok().body(null);
    }
}
