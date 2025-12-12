package com.example.FoodDelivery.domain.res.websocket;

import java.math.BigDecimal;
import java.time.Instant;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DriverLocationUpdate {
    private Long driverId;
    private Long orderId;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private Instant timestamp;
}
