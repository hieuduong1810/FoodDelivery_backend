package com.example.FoodDelivery.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.FoodDelivery.domain.TelegramVerification;

@Repository
public interface TelegramVerificationRepository extends JpaRepository<TelegramVerification, Long> {
    Optional<TelegramVerification> findTopByPhoneNumberOrderByCreatedAtDesc(String phoneNumber);

    Optional<TelegramVerification> findByPhoneNumberAndOtpCodeAndIsVerifiedFalse(String phoneNumber, String otpCode);

    Optional<TelegramVerification> findByTelegramChatId(Long chatId);
}
