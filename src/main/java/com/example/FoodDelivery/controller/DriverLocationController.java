package com.example.FoodDelivery.controller;

import java.math.BigDecimal;
import java.security.Principal;
import java.time.Instant;

import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;

import com.example.FoodDelivery.domain.res.websocket.DriverLocationUpdate;
import com.example.FoodDelivery.service.DriverProfileService;
import com.example.FoodDelivery.service.OrderService;
import com.example.FoodDelivery.service.WebSocketService;
import com.example.FoodDelivery.util.error.IdInvalidException;

import lombok.extern.slf4j.Slf4j;

@Controller
@Slf4j
public class DriverLocationController {

    private final WebSocketService webSocketService;
    private final DriverProfileService driverProfileService;
    private final OrderService orderService;

    public DriverLocationController(WebSocketService webSocketService,
            DriverProfileService driverProfileService,
            OrderService orderService) {
        this.webSocketService = webSocketService;
        this.driverProfileService = driverProfileService;
        this.orderService = orderService;
    }

    /**
     * Handle driver location updates
     * Driver sends location via: /app/driver/location/{orderId}
     * Customer receives at: /topic/customer/{customerId}/driver-location
     */
    @MessageMapping("/driver/location/{orderId}")
    public void updateDriverLocation(@DestinationVariable("orderId") Long orderId,
            @Payload DriverLocationUpdate locationUpdate,
            Principal principal) {
        String authenticatedUser = principal != null ? principal.getName() : "anonymous";
        log.debug("Received location update for order {} (authenticated user: {})",
                orderId, authenticatedUser);

        // Verify order exists
        var order = orderService.getOrderById(orderId);
        if (order == null) {
            log.error("Order {} not found", orderId);
            return;
        }

        // Verify driver is authenticated and assigned to this order
        if (order.getDriver() == null) {
            log.error("No driver assigned to order {}", orderId);
            return;
        }

        // Set timestamp
        locationUpdate.setTimestamp(Instant.now());

        // Update driver profile location in database
        try {
            driverProfileService.updateDriverLocation(
                    order.getDriver().getId(),
                    locationUpdate.getLatitude(),
                    locationUpdate.getLongitude());
            log.debug("Updated driver {} location in database: {}, {}",
                    order.getDriver().getId(),
                    locationUpdate.getLatitude(),
                    locationUpdate.getLongitude());
        } catch (IdInvalidException e) {
            log.error("Failed to update driver location: {}", e.getMessage());
        }

        // Broadcast location to customer
        webSocketService.broadcastDriverLocation(order.getCustomer().getId(), locationUpdate);
    }
}
