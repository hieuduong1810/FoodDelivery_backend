package com.example.FoodDelivery.service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.FoodDelivery.domain.Order;
import com.example.FoodDelivery.domain.Restaurant;
import com.example.FoodDelivery.domain.User;
import com.example.FoodDelivery.domain.res.ResultPaginationDTO;
import com.example.FoodDelivery.repository.OrderRepository;
import com.example.FoodDelivery.util.error.IdInvalidException;

@Service
public class OrderService {
    private final OrderRepository orderRepository;
    private final UserService userService;
    private final RestaurantService restaurantService;

    public OrderService(OrderRepository orderRepository, UserService userService, RestaurantService restaurantService) {
        this.orderRepository = orderRepository;
        this.userService = userService;
        this.restaurantService = restaurantService;
    }

    public Order getOrderById(Long id) {
        Optional<Order> orderOpt = this.orderRepository.findById(id);
        return orderOpt.orElse(null);
    }

    public List<Order> getOrdersByCustomerId(Long customerId) {
        return this.orderRepository.findByCustomerId(customerId);
    }

    public List<Order> getOrdersByRestaurantId(Long restaurantId) {
        return this.orderRepository.findByRestaurantId(restaurantId);
    }

    public List<Order> getOrdersByDriverId(Long driverId) {
        return this.orderRepository.findByDriverId(driverId);
    }

    public List<Order> getOrdersByStatus(String orderStatus) {
        return this.orderRepository.findByOrderStatus(orderStatus);
    }

    public List<Order> getOrdersByCustomerIdAndStatus(Long customerId, String orderStatus) {
        return this.orderRepository.findByCustomerIdAndOrderStatus(customerId, orderStatus);
    }

    @Transactional
    public Order createOrder(Order order) throws IdInvalidException {
        // check customer exists
        if (order.getCustomer() != null) {
            User customer = this.userService.getUserById(order.getCustomer().getId());
            if (customer == null) {
                throw new IdInvalidException("Customer not found with id: " + order.getCustomer().getId());
            }
            order.setCustomer(customer);
        } else {
            throw new IdInvalidException("Customer is required");
        }

        // check restaurant exists
        if (order.getRestaurant() != null) {
            Restaurant restaurant = this.restaurantService.getRestaurantById(order.getRestaurant().getId());
            if (restaurant == null) {
                throw new IdInvalidException("Restaurant not found with id: " + order.getRestaurant().getId());
            }
            order.setRestaurant(restaurant);
        } else {
            throw new IdInvalidException("Restaurant is required");
        }

        // check driver exists (if assigned)
        if (order.getDriver() != null) {
            User driver = this.userService.getUserById(order.getDriver().getId());
            if (driver == null) {
                throw new IdInvalidException("Driver not found with id: " + order.getDriver().getId());
            }
            order.setDriver(driver);
        }

        // set default values
        if (order.getOrderStatus() == null) {
            order.setOrderStatus("PENDING");
        }
        if (order.getPaymentStatus() == null) {
            order.setPaymentStatus("UNPAID");
        }
        order.setCreatedAt(Instant.now());

        return orderRepository.save(order);
    }

    @Transactional
    public Order updateOrder(Order order) throws IdInvalidException {
        // check id
        Order currentOrder = getOrderById(order.getId());
        if (currentOrder == null) {
            throw new IdInvalidException("Order not found with id: " + order.getId());
        }

        // update fields
        if (order.getOrderStatus() != null) {
            currentOrder.setOrderStatus(order.getOrderStatus());

            // set deliveredAt when status is DELIVERED
            if ("DELIVERED".equals(order.getOrderStatus()) && currentOrder.getDeliveredAt() == null) {
                currentOrder.setDeliveredAt(Instant.now());
            }
        }

        if (order.getDriver() != null) {
            User driver = this.userService.getUserById(order.getDriver().getId());
            if (driver == null) {
                throw new IdInvalidException("Driver not found with id: " + order.getDriver().getId());
            }
            currentOrder.setDriver(driver);
        }

        if (order.getDeliveryAddress() != null) {
            currentOrder.setDeliveryAddress(order.getDeliveryAddress());
        }
        if (order.getDeliveryLatitude() != null) {
            currentOrder.setDeliveryLatitude(order.getDeliveryLatitude());
        }
        if (order.getDeliveryLongitude() != null) {
            currentOrder.setDeliveryLongitude(order.getDeliveryLongitude());
        }
        if (order.getSpecialInstructions() != null) {
            currentOrder.setSpecialInstructions(order.getSpecialInstructions());
        }
        if (order.getSubtotal() != null) {
            currentOrder.setSubtotal(order.getSubtotal());
        }
        if (order.getDeliveryFee() != null) {
            currentOrder.setDeliveryFee(order.getDeliveryFee());
        }
        if (order.getTotalAmount() != null) {
            currentOrder.setTotalAmount(order.getTotalAmount());
        }
        if (order.getPaymentMethod() != null) {
            currentOrder.setPaymentMethod(order.getPaymentMethod());
        }
        if (order.getPaymentStatus() != null) {
            currentOrder.setPaymentStatus(order.getPaymentStatus());
        }
        if (order.getCancellationReason() != null) {
            currentOrder.setCancellationReason(order.getCancellationReason());
        }

        return orderRepository.save(currentOrder);
    }

    @Transactional
    public Order assignDriver(Long orderId, Long driverId) throws IdInvalidException {
        Order order = getOrderById(orderId);
        if (order == null) {
            throw new IdInvalidException("Order not found with id: " + orderId);
        }

        User driver = this.userService.getUserById(driverId);
        if (driver == null) {
            throw new IdInvalidException("Driver not found with id: " + driverId);
        }

        order.setDriver(driver);
        order.setOrderStatus("ASSIGNED");
        return orderRepository.save(order);
    }

    @Transactional
    public Order updateOrderStatus(Long orderId, String status) throws IdInvalidException {
        Order order = getOrderById(orderId);
        if (order == null) {
            throw new IdInvalidException("Order not found with id: " + orderId);
        }

        order.setOrderStatus(status);

        // set deliveredAt when status is DELIVERED
        if ("DELIVERED".equals(status) && order.getDeliveredAt() == null) {
            order.setDeliveredAt(Instant.now());
        }

        return orderRepository.save(order);
    }

    @Transactional
    public Order cancelOrder(Long orderId, String cancellationReason) throws IdInvalidException {
        Order order = getOrderById(orderId);
        if (order == null) {
            throw new IdInvalidException("Order not found with id: " + orderId);
        }

        if ("DELIVERED".equals(order.getOrderStatus()) || "CANCELLED".equals(order.getOrderStatus())) {
            throw new IdInvalidException("Cannot cancel order with status: " + order.getOrderStatus());
        }

        order.setOrderStatus("CANCELLED");
        order.setCancellationReason(cancellationReason);
        return orderRepository.save(order);
    }

    public ResultPaginationDTO getAllOrders(Specification<Order> spec, Pageable pageable) {
        Page<Order> page = this.orderRepository.findAll(spec, pageable);
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

    public void deleteOrder(Long id) {
        this.orderRepository.deleteById(id);
    }
}
