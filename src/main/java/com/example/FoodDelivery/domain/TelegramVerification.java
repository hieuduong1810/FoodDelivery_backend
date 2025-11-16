package com.example.FoodDelivery.domain;

import java.time.Instant;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "telegram_verifications")
@Getter
@Setter
@NoArgsConstructor
public class TelegramVerification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String phoneNumber;
    private String otpCode;
    private Long telegramChatId;
    private String telegramUsername;
    private Boolean isVerified;
    private Instant expiresAt;
    private Instant createdAt;
    private Instant verifiedAt;

    public TelegramVerification(String phoneNumber, String otpCode, Instant expiresAt) {
        this.phoneNumber = phoneNumber;
        this.otpCode = otpCode;
        this.expiresAt = expiresAt;
        this.isVerified = false;
        this.createdAt = Instant.now();
    }

    public boolean isExpired() {
        return Instant.now().isAfter(expiresAt);
    }
}
