package com.example.FoodDelivery.domain.res.telegram;

import java.time.Instant;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class ResVerificationDTO {
    private String message;
    private String phoneNumber;
    private Boolean isVerified;
    private Instant verifiedAt;
    private Long telegramChatId;
}
