package com.example.FoodDelivery.service;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import com.example.FoodDelivery.domain.res.order.ResOrderDTO;
import com.example.FoodDelivery.domain.res.websocket.OrderNotification;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class WebSocketService {

    private final SimpMessagingTemplate messagingTemplate;

    public WebSocketService(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    /**
     * Send notification to specific restaurant when new order is created
     */
    public void notifyRestaurantNewOrder(Long restaurantId, ResOrderDTO order) {
        OrderNotification notification = new OrderNotification(
                "NEW_ORDER",
                order.getId(),
                "New order received from customer",
                order);

        String destination = "/topic/restaurant/" + restaurantId + "/orders";
        messagingTemplate.convertAndSend(destination, notification);
        log.info("Sent NEW_ORDER notification to restaurant {} at {}", restaurantId, destination);
    }

    /**
     * Send notification to specific driver when order is assigned
     */
    public void notifyDriverOrderAssigned(Long driverId, ResOrderDTO order) {
        OrderNotification notification = new OrderNotification(
                "ORDER_ASSIGNED",
                order.getId(),
                "New order assigned to you",
                order);

        String destination = "/topic/driver/" + driverId + "/orders";
        messagingTemplate.convertAndSend(destination, notification);
        log.info("Sent ORDER_ASSIGNED notification to driver {} at {}", driverId, destination);
    }

    /**
     * Send notification to customer about order status update
     */
    public void notifyCustomerOrderUpdate(Long customerId, ResOrderDTO order, String message) {
        OrderNotification notification = new OrderNotification(
                "ORDER_UPDATE",
                order.getId(),
                message,
                order);

        String destination = "/topic/customer/" + customerId + "/orders";
        messagingTemplate.convertAndSend(destination, notification);
        log.info("Sent ORDER_UPDATE notification to customer {} at {}", customerId, destination);
    }

    /**
     * Broadcast order status change to all relevant parties
     */
    public void broadcastOrderStatusChange(ResOrderDTO order) {
        OrderNotification notification = new OrderNotification(
                "ORDER_STATUS_CHANGED",
                order.getId(),
                "Order status updated to: " + order.getOrderStatus(),
                order);

        // Notify customer
        if (order.getCustomer() != null) {
            messagingTemplate.convertAndSend(
                    "/topic/customer/" + order.getCustomer().getId() + "/orders",
                    notification);
        }

        // Notify restaurant
        if (order.getRestaurant() != null) {
            messagingTemplate.convertAndSend(
                    "/topic/restaurant/" + order.getRestaurant().getId() + "/orders",
                    notification);
        }

        // Notify driver if assigned
        if (order.getDriver() != null) {
            messagingTemplate.convertAndSend(
                    "/topic/driver/" + order.getDriver().getId() + "/orders",
                    notification);
        }

        log.info("Broadcasted ORDER_STATUS_CHANGED for order {}", order.getId());
    }
}
