package com.example.FoodDelivery.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.example.FoodDelivery.domain.DriverProfile;

import java.util.Optional;

@Repository
public interface DriverProfileRepository
        extends JpaRepository<DriverProfile, Long>, JpaSpecificationExecutor<DriverProfile> {
    Optional<DriverProfile> findByUserId(Long userId);

    boolean existsByUserId(Long userId);
}
