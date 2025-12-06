package com.example.FoodDelivery.domain.res.websocket;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderNotification {
    private String type; // NEW_ORDER, ORDER_ASSIGNED, ORDER_ACCEPTED, etc.
    private Long orderId;
    private String message;
    private Object data; // Order details or any additional data
}
