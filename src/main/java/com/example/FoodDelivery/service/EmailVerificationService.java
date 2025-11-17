package com.example.FoodDelivery.service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.Random;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.FoodDelivery.domain.EmailVerification;
import com.example.FoodDelivery.domain.User;
import com.example.FoodDelivery.repository.EmailVerificationRepository;
import com.example.FoodDelivery.repository.UserRepository;
import com.example.FoodDelivery.util.error.IdInvalidException;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class EmailVerificationService {

    private final EmailVerificationRepository emailVerificationRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;
    private final Random random = new Random();

    public EmailVerificationService(
            EmailVerificationRepository emailVerificationRepository,
            UserRepository userRepository,
            EmailService emailService) {
        this.emailVerificationRepository = emailVerificationRepository;
        this.userRepository = userRepository;
        this.emailService = emailService;
    }

    /**
     * Send verification email with OTP to user
     * 
     * @param user User to send verification email
     * @throws IdInvalidException if user already verified
     */
    @Transactional
    public void sendVerificationEmail(User user) throws IdInvalidException {
        if (user == null) {
            throw new IdInvalidException("User không được để trống");
        }

        if (user.getIsActive() != null && user.getIsActive()) {
            throw new IdInvalidException("Tài khoản đã được kích hoạt");
        }

        // Invalidate any existing unverified OTP for this user
        emailVerificationRepository.findByUserAndIsVerifiedFalse(user)
                .ifPresent(existing -> {
                    emailVerificationRepository.delete(existing);
                    log.info("Deleted existing unverified OTP for user: {}", user.getEmail());
                });

        // Generate 6-digit OTP
        String otpCode = generateOtp();

        // Set expiration to 15 minutes from now
        Instant expiresAt = Instant.now().plus(15, ChronoUnit.MINUTES);

        // Create verification record
        EmailVerification verification = new EmailVerification(user, otpCode, expiresAt);
        emailVerificationRepository.save(verification);

        // Send email
        try {
            emailService.sendVerificationEmail(
                    user.getEmail(),
                    user.getName() != null ? user.getName() : "User",
                    otpCode);
            log.info("Verification OTP email sent successfully to: {}", user.getEmail());
        } catch (Exception e) {
            log.error("Failed to send verification email to: {}", user.getEmail(), e);
            throw new IdInvalidException("Không thể gửi email xác thực. Vui lòng thử lại sau.");
        }
    }

    /**
     * Resend verification email with new OTP
     * 
     * @param email User email
     * @throws IdInvalidException if user not found or already verified
     */
    @Transactional
    public void resendVerificationEmail(String email) throws IdInvalidException {
        User user = userRepository.findByEmail(email);

        if (user == null) {
            throw new IdInvalidException("Không tìm thấy tài khoản với email: " + email);
        }

        if (user.getIsActive() != null && user.getIsActive()) {
            throw new IdInvalidException("Tài khoản đã được kích hoạt");
        }

        // Check for existing unverified verification
        Optional<EmailVerification> existingOpt = emailVerificationRepository
                .findTopByEmailAndIsVerifiedFalseOrderByCreatedAtDesc(email);

        if (existingOpt.isPresent()) {
            EmailVerification existing = existingOpt.get();

            // Check if created less than 1 minute ago (prevent spam)
            if (existing.getCreatedAt().isAfter(Instant.now().minus(1, ChronoUnit.MINUTES))) {
                throw new IdInvalidException(
                        "Vui lòng đợi ít nhất 1 phút trước khi gửi lại email xác thực");
            }
        }

        // Send new verification email
        sendVerificationEmail(user);
    }

    /**
     * Verify email with OTP code
     * 
     * @param email   User email
     * @param otpCode OTP code from email
     * @return Verified user
     * @throws IdInvalidException if OTP invalid or expired
     */
    @Transactional
    public User verifyEmail(String email, String otpCode) throws IdInvalidException {
        if (email == null || email.trim().isEmpty()) {
            throw new IdInvalidException("Email không được để trống");
        }

        if (otpCode == null || otpCode.trim().isEmpty()) {
            throw new IdInvalidException("Mã OTP không được để trống");
        }

        Optional<EmailVerification> verificationOpt = emailVerificationRepository
                .findByEmailAndOtpCodeAndIsVerifiedFalse(email, otpCode);

        if (verificationOpt.isEmpty()) {
            throw new IdInvalidException("Mã OTP không hợp lệ hoặc đã được sử dụng");
        }

        EmailVerification verification = verificationOpt.get();

        // Check expiration
        if (verification.isExpired()) {
            throw new IdInvalidException(
                    "Mã OTP đã hết hạn. Vui lòng yêu cầu gửi lại mã mới");
        }

        // Mark as verified
        verification.setIsVerified(true);
        verification.setVerifiedAt(Instant.now());
        emailVerificationRepository.save(verification);

        // Activate user account
        User user = verification.getUser();
        user.setIsActive(true);
        userRepository.save(user);

        // Send welcome email
        try {
            emailService.sendWelcomeEmail(
                    user.getEmail(),
                    user.getName() != null ? user.getName() : "User");
        } catch (Exception e) {
            log.warn("Failed to send welcome email to: {}", user.getEmail(), e);
            // Don't throw exception, welcome email is not critical
        }

        log.info("Email verified successfully for user: {}", user.getEmail());

        return user;
    }

    /**
     * Check if email is verified
     * 
     * @param email User email
     * @return true if verified
     */
    public boolean isEmailVerified(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }

        User user = userRepository.findByEmail(email);

        if (user == null) {
            return false;
        }

        return user.getIsActive() != null && user.getIsActive();
    }

    /**
     * Get pending verification for user
     * 
     * @param user User
     * @return EmailVerification if exists
     */
    public Optional<EmailVerification> getPendingVerification(User user) {
        return emailVerificationRepository.findByUserAndIsVerifiedFalse(user);
    }

    /**
     * Generate 6-digit OTP code
     * 
     * @return OTP code
     */
    private String generateOtp() {
        int otp = 100000 + random.nextInt(900000);
        return String.valueOf(otp);
    }
}
