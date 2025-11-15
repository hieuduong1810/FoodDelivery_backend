package com.example.FoodDelivery.service;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.FoodDelivery.domain.Dish;
import com.example.FoodDelivery.domain.Order;
import com.example.FoodDelivery.domain.OrderItem;
import com.example.FoodDelivery.domain.res.ResultPaginationDTO;
import com.example.FoodDelivery.repository.OrderItemRepository;
import com.example.FoodDelivery.util.error.IdInvalidException;

@Service
public class OrderItemService {
    private final OrderItemRepository orderItemRepository;
    private final OrderService orderService;
    private final DishService dishService;

    public OrderItemService(OrderItemRepository orderItemRepository, OrderService orderService,
            DishService dishService) {
        this.orderItemRepository = orderItemRepository;
        this.orderService = orderService;
        this.dishService = dishService;
    }

    public OrderItem getOrderItemById(Long id) {
        Optional<OrderItem> orderItemOpt = this.orderItemRepository.findById(id);
        return orderItemOpt.orElse(null);
    }

    public List<OrderItem> getOrderItemsByOrderId(Long orderId) {
        return this.orderItemRepository.findByOrderId(orderId);
    }

    public List<OrderItem> getOrderItemsByDishId(Long dishId) {
        return this.orderItemRepository.findByDishId(dishId);
    }

    @Transactional
    public OrderItem createOrderItem(OrderItem orderItem) throws IdInvalidException {
        // check order exists
        if (orderItem.getOrder() != null) {
            Order order = this.orderService.getOrderById(orderItem.getOrder().getId());
            if (order == null) {
                throw new IdInvalidException("Order not found with id: " + orderItem.getOrder().getId());
            }
            orderItem.setOrder(order);
        } else {
            throw new IdInvalidException("Order is required");
        }

        // check dish exists
        if (orderItem.getDish() != null) {
            Dish dish = this.dishService.getDishById(orderItem.getDish().getId());
            if (dish == null) {
                throw new IdInvalidException("Dish not found with id: " + orderItem.getDish().getId());
            }
            orderItem.setDish(dish);

            // set price at purchase from current dish price if not provided
            if (orderItem.getPriceAtPurchase() == null) {
                orderItem.setPriceAtPurchase(dish.getPrice());
            }
        } else {
            throw new IdInvalidException("Dish is required");
        }

        // validate quantity
        if (orderItem.getQuantity() == null || orderItem.getQuantity() <= 0) {
            throw new IdInvalidException("Quantity must be greater than 0");
        }

        return orderItemRepository.save(orderItem);
    }

    @Transactional
    public OrderItem updateOrderItem(OrderItem orderItem) throws IdInvalidException {
        // check id
        OrderItem currentOrderItem = getOrderItemById(orderItem.getId());
        if (currentOrderItem == null) {
            throw new IdInvalidException("Order item not found with id: " + orderItem.getId());
        }

        // update quantity
        if (orderItem.getQuantity() != null) {
            if (orderItem.getQuantity() <= 0) {
                throw new IdInvalidException("Quantity must be greater than 0");
            }
            currentOrderItem.setQuantity(orderItem.getQuantity());
        }

        // update price at purchase
        if (orderItem.getPriceAtPurchase() != null) {
            currentOrderItem.setPriceAtPurchase(orderItem.getPriceAtPurchase());
        }

        return orderItemRepository.save(currentOrderItem);
    }

    public ResultPaginationDTO getAllOrderItems(Specification<OrderItem> spec, Pageable pageable) {
        Page<OrderItem> page = this.orderItemRepository.findAll(spec, pageable);
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

    @Transactional
    public void deleteOrderItem(Long id) {
        this.orderItemRepository.deleteById(id);
    }
}
