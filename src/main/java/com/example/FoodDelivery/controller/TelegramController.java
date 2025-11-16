package com.example.FoodDelivery.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.FoodDelivery.domain.TelegramVerification;
import com.example.FoodDelivery.domain.req.telegram.ReqRequestOtpDTO;
import com.example.FoodDelivery.domain.req.telegram.ReqSendNotificationDTO;
import com.example.FoodDelivery.domain.req.telegram.ReqVerifyOtpDTO;
import com.example.FoodDelivery.domain.res.telegram.ResOtpDTO;
import com.example.FoodDelivery.domain.res.telegram.ResVerificationDTO;
import com.example.FoodDelivery.service.TelegramVerificationService;
import com.example.FoodDelivery.util.annotation.ApiMessage;
import com.example.FoodDelivery.util.error.IdInvalidException;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/telegram")
@Tag(name = "Telegram Verification", description = "API xác thực số điện thoại qua Telegram")
public class TelegramController {

    private final TelegramVerificationService verificationService;

    public TelegramController(TelegramVerificationService verificationService) {
        this.verificationService = verificationService;
    }

    @PostMapping("/request-otp")
    @ApiMessage("Yêu cầu mã OTP qua Telegram")
    @Operation(summary = "Yêu cầu mã OTP", description = "Gửi mã OTP 6 chữ số qua Telegram Bot. Số điện thoại phải được liên kết với Telegram trước.")
    public ResponseEntity<ResOtpDTO> requestOtp(@Valid @RequestBody ReqRequestOtpDTO request)
            throws IdInvalidException {

        String otpCode = verificationService.requestOtp(request.getPhoneNumber());

        // Get chat ID to verify it's linked
        var chatId = verificationService.getTelegramChatId(request.getPhoneNumber());

        ResOtpDTO response = new ResOtpDTO(
                "Mã OTP đã được gửi đến Telegram của bạn. Vui lòng kiểm tra tin nhắn từ bot.",
                request.getPhoneNumber(),
                java.time.Instant.now().plus(5, java.time.temporal.ChronoUnit.MINUTES),
                chatId.isPresent() ? "Linked" : "Not linked");

        return ResponseEntity.ok(response);
    }

    @PostMapping("/verify-otp")
    @ApiMessage("Xác thực mã OTP")
    @Operation(summary = "Xác thực mã OTP", description = "Xác nhận mã OTP nhận được từ Telegram để hoàn tất xác thực số điện thoại.")
    public ResponseEntity<ResVerificationDTO> verifyOtp(@Valid @RequestBody ReqVerifyOtpDTO request)
            throws IdInvalidException {

        TelegramVerification verification = verificationService.verifyOtp(
                request.getPhoneNumber(),
                request.getOtpCode());

        ResVerificationDTO response = new ResVerificationDTO(
                "Xác thực số điện thoại thành công!",
                verification.getPhoneNumber(),
                verification.getIsVerified(),
                verification.getVerifiedAt(),
                verification.getTelegramChatId());

        return ResponseEntity.ok(response);
    }

    @PostMapping("/send-notification")
    @ApiMessage("Gửi thông báo qua Telegram")
    @Operation(summary = "Gửi thông báo", description = "Gửi thông báo tùy chỉnh đến người dùng qua Telegram Bot.")
    public ResponseEntity<String> sendNotification(@Valid @RequestBody ReqSendNotificationDTO request) {

        verificationService.sendNotification(request.getPhoneNumber(), request.getMessage());

        return ResponseEntity.ok("Thông báo đã được gửi thành công");
    }

    @PostMapping("/check-verification")
    @ApiMessage("Kiểm tra trạng thái xác thực")
    @Operation(summary = "Kiểm tra xác thực", description = "Kiểm tra xem số điện thoại đã được xác thực hay chưa.")
    public ResponseEntity<Boolean> checkVerification(@Valid @RequestBody ReqRequestOtpDTO request) {

        boolean isVerified = verificationService.isPhoneVerified(request.getPhoneNumber());

        return ResponseEntity.ok(isVerified);
    }
}
