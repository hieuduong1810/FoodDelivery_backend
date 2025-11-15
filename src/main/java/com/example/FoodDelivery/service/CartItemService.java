package com.example.FoodDelivery.service;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.example.FoodDelivery.domain.Cart;
import com.example.FoodDelivery.domain.CartItem;
import com.example.FoodDelivery.domain.Dish;
import com.example.FoodDelivery.domain.res.ResultPaginationDTO;
import com.example.FoodDelivery.repository.CartItemRepository;
import com.example.FoodDelivery.util.error.IdInvalidException;

@Service
public class CartItemService {
    private final CartItemRepository cartItemRepository;
    private final CartService cartService;
    private final DishService dishService;

    public CartItemService(CartItemRepository cartItemRepository, CartService cartService, DishService dishService) {
        this.cartItemRepository = cartItemRepository;
        this.cartService = cartService;
        this.dishService = dishService;
    }

    public CartItem getCartItemById(Long id) {
        Optional<CartItem> cartItemOpt = this.cartItemRepository.findById(id);
        return cartItemOpt.orElse(null);
    }

    public List<CartItem> getCartItemsByCartId(Long cartId) {
        return this.cartItemRepository.findByCartId(cartId);
    }

    public CartItem getCartItemByCartIdAndDishId(Long cartId, Long dishId) {
        Optional<CartItem> cartItemOpt = this.cartItemRepository.findByCartIdAndDishId(cartId, dishId);
        return cartItemOpt.orElse(null);
    }

    public CartItem createCartItem(CartItem cartItem) throws IdInvalidException {
        // check cart exists
        if (cartItem.getCart() != null) {
            Cart cart = this.cartService.getCartById(cartItem.getCart().getId());
            if (cart == null) {
                throw new IdInvalidException("Cart not found with id: " + cartItem.getCart().getId());
            }
            cartItem.setCart(cart);
        } else {
            throw new IdInvalidException("Cart is required");
        }

        // check dish exists
        if (cartItem.getDish() != null) {
            Dish dish = this.dishService.getDishById(cartItem.getDish().getId());
            if (dish == null) {
                throw new IdInvalidException("Dish not found with id: " + cartItem.getDish().getId());
            }
            cartItem.setDish(dish);
        } else {
            throw new IdInvalidException("Dish is required");
        }

        // check if item already exists in cart
        CartItem existingItem = this.getCartItemByCartIdAndDishId(
                cartItem.getCart().getId(),
                cartItem.getDish().getId());
        if (existingItem != null) {
            // update quantity instead of creating new item
            existingItem.setQuantity(existingItem.getQuantity() + cartItem.getQuantity());
            return cartItemRepository.save(existingItem);
        }

        return cartItemRepository.save(cartItem);
    }

    public CartItem updateCartItem(CartItem cartItem) throws IdInvalidException {
        // check id
        CartItem currentCartItem = getCartItemById(cartItem.getId());
        if (currentCartItem == null) {
            throw new IdInvalidException("Cart item not found with id: " + cartItem.getId());
        }

        // update quantity
        if (cartItem.getQuantity() != null) {
            if (cartItem.getQuantity() <= 0) {
                throw new IdInvalidException("Quantity must be greater than 0");
            }
            currentCartItem.setQuantity(cartItem.getQuantity());
        }

        return cartItemRepository.save(currentCartItem);
    }

    public ResultPaginationDTO getAllCartItems(Specification<CartItem> spec, Pageable pageable) {
        Page<CartItem> page = this.cartItemRepository.findAll(spec, pageable);
        ResultPaginationDTO result = new ResultPaginationDTO();
        ResultPaginationDTO.Meta meta = new ResultPaginationDTO.Meta();
        meta.setPage(pageable.getPageNumber() + 1);
        meta.setPageSize(pageable.getPageSize());
        meta.setTotal(page.getTotalElements());
        meta.setPages(page.getTotalPages());
        result.setMeta(meta);
        result.setResult(page.getContent());
        return result;
    }

    public void deleteCartItem(Long id) {
        this.cartItemRepository.deleteById(id);
    }
}
