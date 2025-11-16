package com.example.FoodDelivery.service;

import java.time.Instant;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import com.example.FoodDelivery.domain.TelegramVerification;
import com.example.FoodDelivery.repository.TelegramVerificationRepository;

import lombok.extern.slf4j.Slf4j;

@Service
@ConditionalOnProperty(prefix = "telegram.bot", name = "enabled", havingValue = "true")
@Slf4j
public class TelegramBotService extends TelegramLongPollingBot {

    @Value("${telegram.bot.token}")
    private String botToken;

    @Value("${telegram.bot.username}")
    private String botUsername;

    private final TelegramVerificationRepository verificationRepository;

    public TelegramBotService(TelegramVerificationRepository verificationRepository) {
        this.verificationRepository = verificationRepository;
    }

    @Override
    public String getBotUsername() {
        return botUsername;
    }

    @Override
    public String getBotToken() {
        return botToken;
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            Long chatId = update.getMessage().getChatId();
            String messageText = update.getMessage().getText();
            String username = update.getMessage().getFrom().getUserName();

            log.info("Received message from chatId {}: {}", chatId, messageText);

            if (messageText.startsWith("/start")) {
                handleStartCommand(chatId, username);
            } else if (messageText.startsWith("/verify")) {
                handleVerifyCommand(chatId, username, messageText);
            } else {
                sendMessage(chatId, "Xin chào! Sử dụng /start để bắt đầu hoặc /verify <số_điện_thoại> để xác thực.");
            }
        }
    }

    private void handleStartCommand(Long chatId, String username) {
        // Save chat ID for future use
        log.info("User {} started bot with chatId: {}", username, chatId);

        String welcomeMessage = String.format("""
                🎉 Chào mừng bạn đến với Food Delivery Bot!

                👤 Telegram: @%s
                🆔 Chat ID: %d

                📱 Để xác thực số điện thoại:
                1. Gửi lệnh: /verify <số_điện_thoại>
                   Ví dụ: /verify 0912345678

                2. Sau đó yêu cầu mã OTP từ ứng dụng

                3. Bạn sẽ nhận mã OTP tại đây

                4. Nhập mã vào ứng dụng để hoàn tất

                💡 Lệnh có sẵn:
                /start - Xem hướng dẫn này
                /verify <số_điện_thoại> - Liên kết số điện thoại
                """,
                username != null ? username : "Unknown",
                chatId);
        sendMessage(chatId, welcomeMessage);
    }

    private void handleVerifyCommand(Long chatId, String username, String messageText) {
        String[] parts = messageText.split(" ");
        if (parts.length < 2) {
            sendMessage(chatId, "❌ Vui lòng cung cấp số điện thoại. Ví dụ: /verify 0912345678");
            return;
        }

        String phoneNumber = parts[1].trim();

        // Validate phone number format
        if (!phoneNumber.matches("^(0|\\+84)[0-9]{9}$")) {
            sendMessage(chatId, "❌ Số điện thoại không hợp lệ. Vui lòng nhập đúng định dạng: 0912345678");
            return;
        }

        // Check if there's any verification record for this phone number
        Optional<TelegramVerification> verificationOpt = verificationRepository
                .findTopByPhoneNumberOrderByCreatedAtDesc(phoneNumber);

        if (verificationOpt.isPresent()) {
            // Update existing record with chat ID
            TelegramVerification verification = verificationOpt.get();
            verification.setTelegramChatId(chatId);
            verification.setTelegramUsername(username);
            verificationRepository.save(verification);

            String message = String.format("""
                    ✅ Đã liên kết số điện thoại %s với Telegram!

                    👤 Telegram: @%s
                    🆔 Chat ID: %d

                    📱 Bước tiếp theo:
                    1. Yêu cầu mã OTP từ ứng dụng
                    2. Bạn sẽ nhận mã OTP tại đây
                    3. Nhập mã vào ứng dụng để xác thực
                    """, phoneNumber, username != null ? username : "Unknown", chatId);
            sendMessage(chatId, message);
        } else {
            // Create new verification record for linking
            Instant expiresAt = Instant.now().plus(24, java.time.temporal.ChronoUnit.HOURS);
            TelegramVerification verification = new TelegramVerification(phoneNumber, "", expiresAt);
            verification.setTelegramChatId(chatId);
            verification.setTelegramUsername(username);
            verificationRepository.save(verification);

            String message = String.format("""
                    ✅ Đã tạo liên kết mới cho số điện thoại %s!

                    👤 Telegram: @%s
                    🆔 Chat ID: %d

                    📱 Bước tiếp theo:
                    1. Yêu cầu mã OTP từ ứng dụng (API: POST /api/v1/telegram/request-otp)
                    2. Bạn sẽ nhận mã OTP tại đây
                    3. Nhập mã vào ứng dụng để xác thực

                    💡 Liên kết này có hiệu lực trong 24 giờ.
                    """, phoneNumber, username != null ? username : "Unknown", chatId);
            sendMessage(chatId, message);

            log.info("Created new verification link for phone {} with chatId {}", phoneNumber, chatId);
        }
    }

    public void sendOtpCode(Long chatId, String phoneNumber, String otpCode) {
        String message = String.format("""
                🔐 Mã xác thực của bạn:

                📱 Số điện thoại: %s
                🔢 Mã OTP: %s

                ⏰ Mã có hiệu lực trong 5 phút.
                ⚠️ Không chia sẻ mã này với bất kỳ ai!
                """, phoneNumber, otpCode);

        sendMessage(chatId, message);
    }

    public void sendVerificationSuccess(Long chatId, String phoneNumber) {
        String message = String.format("""
                ✅ Xác thực thành công!

                📱 Số điện thoại %s đã được xác thực.
                🎉 Bạn có thể sử dụng đầy đủ tính năng của ứng dụng.
                """, phoneNumber);

        sendMessage(chatId, message);
    }

    public void sendNotification(Long chatId, String message) {
        sendMessage(chatId, "📢 " + message);
    }

    private void sendMessage(Long chatId, String text) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId.toString());
        message.setText(text);

        try {
            execute(message);
            log.info("Message sent to chatId {}", chatId);
        } catch (TelegramApiException e) {
            log.error("Failed to send message to chatId {}: {}", chatId, e.getMessage());
        }
    }

    public boolean isChatIdValid(Long chatId) {
        if (chatId == null) {
            return false;
        }

        try {
            SendMessage testMessage = new SendMessage();
            testMessage.setChatId(chatId.toString());
            testMessage.setText("Test connection");
            // This will throw exception if chat_id is invalid
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
