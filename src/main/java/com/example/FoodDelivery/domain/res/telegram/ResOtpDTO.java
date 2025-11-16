package com.example.FoodDelivery.domain.res.telegram;

import java.time.Instant;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class ResOtpDTO {
    private String message;
    private String phoneNumber;
    private Instant expiresAt;
    private String telegramUsername;
}
