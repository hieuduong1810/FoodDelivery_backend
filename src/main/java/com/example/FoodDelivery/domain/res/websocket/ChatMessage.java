package com.example.FoodDelivery.domain.res.websocket;

import java.time.Instant;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChatMessage {
    private Long orderId;
    private Long senderId;
    private String senderName;
    private String senderType; // "DRIVER" or "CUSTOMER"
    private String message;
    private Instant timestamp;
    private String messageType; // "TEXT", "IMAGE", "LOCATION"
}
