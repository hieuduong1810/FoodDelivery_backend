package com.example.FoodDelivery.controller;

import java.security.Principal;
import java.time.Instant;

import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import com.example.FoodDelivery.domain.res.websocket.ChatMessage;
import com.example.FoodDelivery.service.OrderService;

import lombok.extern.slf4j.Slf4j;

@Controller
@Slf4j
public class ChatController {

    private final SimpMessagingTemplate messagingTemplate;
    private final OrderService orderService;

    public ChatController(SimpMessagingTemplate messagingTemplate, OrderService orderService) {
        this.messagingTemplate = messagingTemplate;
        this.orderService = orderService;
    }

    /**
     * Handle chat messages sent from client
     * Endpoint: /app/chat/{orderId}
     */
    @MessageMapping("/chat/{orderId}")
    public void handleChatMessage(@DestinationVariable("orderId") Long orderId,
            @Payload ChatMessage message,
            Principal principal) {
        String authenticatedUser = principal != null ? principal.getName() : "anonymous";
        log.info("Received chat message for order {} from authenticated user '{}': {} from {} ({})",
                orderId, authenticatedUser, message.getMessage(), message.getSenderName(), message.getSenderType());

        // Set timestamp
        message.setTimestamp(Instant.now());
        message.setOrderId(orderId);

        // Verify order exists
        var order = orderService.getOrderById(orderId);
        if (order == null) {
            log.error("Order {} not found", orderId);
            return;
        }

        // ===== QUEUE VERSION (Point-to-point - Private messaging) =====
        // Issue: Requires proper user authentication with Principal
        // Client must connect with {login: userId} header
        /*
         * // Send to driver via point-to-point queue (private message)
         * if (order.getDriver() != null) {
         * messagingTemplate.convertAndSendToUser(
         * String.valueOf(order.getDriver().getId()),
         * "/queue/chat/order/" + orderId,
         * message);
         * log.info("Sent chat message to driver {} via queue",
         * order.getDriver().getId());
         * }
         * 
         * // Send to customer via point-to-point queue (private message)
         * if (order.getCustomer() != null) {
         * messagingTemplate.convertAndSendToUser(
         * String.valueOf(order.getCustomer().getId()),
         * "/queue/chat/order/" + orderId,
         * message);
         * log.info("Sent chat message to customer {} via queue",
         * order.getCustomer().getId());
         * }
         */

        // ===== TOPIC VERSION (Broadcast - Simpler but less secure) =====
        // Broadcast to all subscribers of this order's chat topic
        // Anyone who subscribes to /topic/chat/order/{orderId} will receive the message
        messagingTemplate.convertAndSend(
                "/topic/chat/order/" + orderId,
                message);
        log.info("Broadcasted chat message to topic /topic/chat/order/{}", orderId);
    }

    /**
     * Handle typing indicator
     * Endpoint: /app/typing/{orderId}
     */
    @MessageMapping("/typing/{orderId}")
    public void handleTypingIndicator(@DestinationVariable("orderId") Long orderId,
            @Payload ChatMessage message,
            Principal principal) {
        String authenticatedUser = principal != null ? principal.getName() : "anonymous";
        log.debug("Received typing indicator for order {} from authenticated user '{}'", orderId, authenticatedUser);

        var order = orderService.getOrderById(orderId);
        if (order == null) {
            return;
        }

        // ===== QUEUE VERSION (Point-to-point) =====
        /*
         * // Broadcast typing indicator to the other party via queue
         * if ("DRIVER".equals(message.getSenderType()) && order.getCustomer() != null)
         * {
         * messagingTemplate.convertAndSendToUser(
         * String.valueOf(order.getCustomer().getId()),
         * "/queue/chat/order/" + orderId + "/typing",
         * message);
         * } else if ("CUSTOMER".equals(message.getSenderType()) && order.getDriver() !=
         * null) {
         * messagingTemplate.convertAndSendToUser(
         * String.valueOf(order.getDriver().getId()),
         * "/queue/chat/order/" + orderId + "/typing",
         * message);
         * }
         */

        // ===== TOPIC VERSION (Broadcast) =====
        // Broadcast typing indicator to all subscribers of this order's chat
        messagingTemplate.convertAndSend(
                "/topic/chat/order/" + orderId + "/typing",
                message);
        log.debug("Broadcasted typing indicator to topic /topic/chat/order/{}/typing", orderId);
    }
}
