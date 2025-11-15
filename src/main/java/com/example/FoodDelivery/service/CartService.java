package com.example.FoodDelivery.service;

import java.time.Instant;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.example.FoodDelivery.domain.Cart;
import com.example.FoodDelivery.domain.Restaurant;
import com.example.FoodDelivery.domain.User;
import com.example.FoodDelivery.domain.res.ResultPaginationDTO;
import com.example.FoodDelivery.repository.CartRepository;
import com.example.FoodDelivery.util.error.IdInvalidException;

@Service
public class CartService {
    private final CartRepository cartRepository;
    private final UserService userService;
    private final RestaurantService restaurantService;

    public CartService(CartRepository cartRepository, UserService userService, RestaurantService restaurantService) {
        this.cartRepository = cartRepository;
        this.userService = userService;
        this.restaurantService = restaurantService;
    }

    public Cart getCartById(Long id) {
        Optional<Cart> cartOpt = this.cartRepository.findById(id);
        return cartOpt.orElse(null);
    }

    public Cart getCartByCustomerId(Long customerId) {
        Optional<Cart> cartOpt = this.cartRepository.findByCustomerId(customerId);
        return cartOpt.orElse(null);
    }

    public Cart getCartByCustomerIdAndRestaurantId(Long customerId, Long restaurantId) {
        Optional<Cart> cartOpt = this.cartRepository.findByCustomerIdAndRestaurantId(customerId, restaurantId);
        return cartOpt.orElse(null);
    }

    public Cart createCart(Cart cart) throws IdInvalidException {
        // check customer exists
        if (cart.getCustomer() != null) {
            User customer = this.userService.getUserById(cart.getCustomer().getId());
            if (customer == null) {
                throw new IdInvalidException("Customer not found with id: " + cart.getCustomer().getId());
            }
            cart.setCustomer(customer);
        } else {
            throw new IdInvalidException("Customer is required");
        }

        // check restaurant exists
        if (cart.getRestaurant() != null) {
            Restaurant restaurant = this.restaurantService.getRestaurantById(cart.getRestaurant().getId());
            if (restaurant == null) {
                throw new IdInvalidException("Restaurant not found with id: " + cart.getRestaurant().getId());
            }
            cart.setRestaurant(restaurant);
        } else {
            throw new IdInvalidException("Restaurant is required");
        }

        cart.setLastUpdated(Instant.now());
        return cartRepository.save(cart);
    }

    public Cart updateCart(Cart cart) throws IdInvalidException {
        // check id
        Cart currentCart = getCartById(cart.getId());
        if (currentCart == null) {
            throw new IdInvalidException("Cart not found with id: " + cart.getId());
        }

        // update restaurant if needed
        if (cart.getRestaurant() != null) {
            Restaurant restaurant = this.restaurantService.getRestaurantById(cart.getRestaurant().getId());
            if (restaurant == null) {
                throw new IdInvalidException("Restaurant not found with id: " + cart.getRestaurant().getId());
            }
            currentCart.setRestaurant(restaurant);
        }

        currentCart.setLastUpdated(Instant.now());
        return cartRepository.save(currentCart);
    }

    public ResultPaginationDTO getAllCarts(Specification<Cart> spec, Pageable pageable) {
        Page<Cart> page = this.cartRepository.findAll(spec, pageable);
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

    public void deleteCart(Long id) {
        this.cartRepository.deleteById(id);
    }
}
