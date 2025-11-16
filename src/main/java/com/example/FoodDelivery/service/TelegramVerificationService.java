package com.example.FoodDelivery.service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.Random;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import com.example.FoodDelivery.domain.TelegramVerification;
import com.example.FoodDelivery.repository.TelegramVerificationRepository;
import com.example.FoodDelivery.util.error.IdInvalidException;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class TelegramVerificationService {

    private final TelegramVerificationRepository verificationRepository;
    private final TelegramBotService telegramBotService;

    public TelegramVerificationService(
            TelegramVerificationRepository verificationRepository,
            Optional<TelegramBotService> telegramBotService) {
        this.verificationRepository = verificationRepository;
        this.telegramBotService = telegramBotService.orElse(null);
    }

    /**
     * Request OTP for phone verification
     * 
     * @param phoneNumber Phone number to verify
     * @return OTP code (for testing purposes, in production should not return this)
     * @throws IdInvalidException if Telegram is not linked
     */
    public String requestOtp(String phoneNumber) throws IdInvalidException {
        // Check if phone is already linked to Telegram
        Optional<TelegramVerification> existingVerification = verificationRepository
                .findTopByPhoneNumberOrderByCreatedAtDesc(phoneNumber);

        if (existingVerification.isEmpty() || existingVerification.get().getTelegramChatId() == null) {
            throw new IdInvalidException(
                    "Số điện thoại chưa được liên kết với Telegram. " +
                            "Vui lòng chat với bot và sử dụng lệnh: /verify " + phoneNumber);
        }

        // Generate 6-digit OTP
        String otpCode = generateOtp();

        // Set expiration to 5 minutes from now
        Instant expiresAt = Instant.now().plus(5, ChronoUnit.MINUTES);

        // Create new verification record
        TelegramVerification verification = new TelegramVerification(phoneNumber, otpCode, expiresAt);
        TelegramVerification existing = existingVerification.get();
        verification.setTelegramChatId(existing.getTelegramChatId());
        verification.setTelegramUsername(existing.getTelegramUsername());

        verificationRepository.save(verification);

        // Send OTP via Telegram
        if (telegramBotService != null && verification.getTelegramChatId() != null) {
            telegramBotService.sendOtpCode(
                    verification.getTelegramChatId(),
                    phoneNumber,
                    otpCode);
            log.info("OTP sent to Telegram chat ID: {}", verification.getTelegramChatId());
        } else {
            log.warn("Telegram bot service not available or chat ID not set");
        }

        return otpCode; // For testing only, remove in production
    }

    /**
     * Verify OTP code
     * 
     * @param phoneNumber Phone number
     * @param otpCode     OTP code to verify
     * @return TelegramVerification if successful
     * @throws IdInvalidException if verification fails
     */
    public TelegramVerification verifyOtp(String phoneNumber, String otpCode) throws IdInvalidException {
        Optional<TelegramVerification> verificationOpt = verificationRepository
                .findByPhoneNumberAndOtpCodeAndIsVerifiedFalse(phoneNumber, otpCode);

        if (verificationOpt.isEmpty()) {
            throw new IdInvalidException("Mã OTP không đúng hoặc đã được sử dụng");
        }

        TelegramVerification verification = verificationOpt.get();

        if (verification.isExpired()) {
            throw new IdInvalidException("Mã OTP đã hết hạn. Vui lòng yêu cầu mã mới");
        }

        // Mark as verified
        verification.setIsVerified(true);
        verification.setVerifiedAt(Instant.now());
        verificationRepository.save(verification);

        // Send success notification via Telegram
        if (telegramBotService != null && verification.getTelegramChatId() != null) {
            telegramBotService.sendVerificationSuccess(
                    verification.getTelegramChatId(),
                    phoneNumber);
        }

        log.info("Phone number {} verified successfully", phoneNumber);
        return verification;
    }

    /**
     * Link phone number to Telegram chat ID
     * 
     * @param phoneNumber Phone number
     * @param chatId      Telegram chat ID
     * @param username    Telegram username
     */
    public void linkPhoneToTelegram(String phoneNumber, Long chatId, String username) {
        // Create initial verification record
        Instant expiresAt = Instant.now().plus(24, ChronoUnit.HOURS);
        TelegramVerification verification = new TelegramVerification(phoneNumber, "", expiresAt);
        verification.setTelegramChatId(chatId);
        verification.setTelegramUsername(username);

        verificationRepository.save(verification);
        log.info("Linked phone {} to Telegram chat ID: {}", phoneNumber, chatId);
    }

    /**
     * Check if phone number is verified
     * 
     * @param phoneNumber Phone number to check
     * @return true if verified
     */
    public boolean isPhoneVerified(String phoneNumber) {
        Optional<TelegramVerification> verificationOpt = verificationRepository
                .findTopByPhoneNumberOrderByCreatedAtDesc(phoneNumber);

        return verificationOpt.isPresent() && verificationOpt.get().getIsVerified();
    }

    /**
     * Get Telegram chat ID for a phone number
     * 
     * @param phoneNumber Phone number
     * @return Chat ID if available
     */
    public Optional<Long> getTelegramChatId(String phoneNumber) {
        Optional<TelegramVerification> verificationOpt = verificationRepository
                .findTopByPhoneNumberOrderByCreatedAtDesc(phoneNumber);

        if (verificationOpt.isPresent() && verificationOpt.get().getTelegramChatId() != null) {
            return Optional.of(verificationOpt.get().getTelegramChatId());
        }

        return Optional.empty();
    }

    /**
     * Send notification to user via Telegram
     * 
     * @param phoneNumber Phone number
     * @param message     Message to send
     */
    public void sendNotification(String phoneNumber, String message) {
        Optional<Long> chatIdOpt = getTelegramChatId(phoneNumber);

        if (chatIdOpt.isPresent() && telegramBotService != null) {
            telegramBotService.sendNotification(chatIdOpt.get(), message);
            log.info("Notification sent to phone {}", phoneNumber);
        } else {
            log.warn("Cannot send notification: Telegram not linked for phone {}", phoneNumber);
        }
    }

    /**
     * Generate random 6-digit OTP
     */
    private String generateOtp() {
        Random random = new Random();
        int otp = 100000 + random.nextInt(900000);
        return String.valueOf(otp);
    }
}
