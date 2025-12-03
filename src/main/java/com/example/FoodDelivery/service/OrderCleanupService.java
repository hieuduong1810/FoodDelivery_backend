package com.example.FoodDelivery.service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.FoodDelivery.domain.Order;
import com.example.FoodDelivery.repository.OrderRepository;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class OrderCleanupService {
    private final OrderRepository orderRepository;

    public OrderCleanupService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    /**
     * Automatically delete expired VNPAY orders that were never paid
     * Runs every 5 minutes
     */
    @Scheduled(fixedRate = 300000) // 5 minutes = 300,000 milliseconds
    @Transactional
    public void cleanupExpiredVNPayOrders() {
        try {
            // Calculate cutoff time: 15 minutes ago
            Instant cutoffTime = Instant.now().minus(15, ChronoUnit.MINUTES);

            // Find all VNPAY orders that are UNPAID and older than 15 minutes
            List<Order> expiredOrders = orderRepository.findByPaymentMethodAndPaymentStatusAndCreatedAtBefore(
                    "VNPAY", "UNPAID", cutoffTime);

            if (!expiredOrders.isEmpty()) {
                log.info("Found {} expired VNPAY orders to cleanup", expiredOrders.size());

                for (Order order : expiredOrders) {
                    log.info("Deleting expired VNPAY order: orderId={}, createdAt={}, age={}minutes",
                            order.getId(),
                            order.getCreatedAt(),
                            ChronoUnit.MINUTES.between(order.getCreatedAt(), Instant.now()));

                    orderRepository.delete(order);
                }

                log.info("Successfully cleaned up {} expired VNPAY orders", expiredOrders.size());
            }
        } catch (Exception e) {
            log.error("Error during VNPAY order cleanup: {}", e.getMessage(), e);
        }
    }
}
