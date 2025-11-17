package com.example.FoodDelivery.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.FoodDelivery.domain.EmailVerification;
import com.example.FoodDelivery.domain.User;

@Repository
public interface EmailVerificationRepository extends JpaRepository<EmailVerification, Long> {
    Optional<EmailVerification> findByEmailAndOtpCodeAndIsVerifiedFalse(String email, String otpCode);

    Optional<EmailVerification> findTopByUserOrderByCreatedAtDesc(User user);

    Optional<EmailVerification> findByUserAndIsVerifiedFalse(User user);

    Optional<EmailVerification> findTopByEmailAndIsVerifiedFalseOrderByCreatedAtDesc(String email);
}
