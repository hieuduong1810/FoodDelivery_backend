package com.example.FoodDelivery.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.FoodDelivery.domain.DriverProfile;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface DriverProfileRepository
                extends JpaRepository<DriverProfile, Long>, JpaSpecificationExecutor<DriverProfile> {
        Optional<DriverProfile> findByUserId(Long userId);

        boolean existsByUserId(Long userId);

        // Find first available driver with COD limit >= amount and status =
        // ONLINE/AVAILABLE, sorted by rating
        @Query(value = "SELECT dp.* FROM driver_profiles dp " +
                        "INNER JOIN users u ON dp.user_id = u.id " +
                        "INNER JOIN wallets w ON u.id = w.user_id " +
                        "WHERE dp.cod_limit >= :amount " +
                        "AND dp.status IN ('ONLINE', 'AVAILABLE') " +
                        "AND w.balance >= 0 " +
                        "ORDER BY dp.average_rating DESC LIMIT 1", nativeQuery = true)
        Optional<DriverProfile> findFirstAvailableDriverByCodLimit(@Param("amount") BigDecimal amount);

        // Find first available driver excluding specific driver IDs
        @Query(value = "SELECT dp.* FROM driver_profiles dp " +
                        "INNER JOIN users u ON dp.user_id = u.id " +
                        "INNER JOIN wallets w ON u.id = w.user_id " +
                        "WHERE dp.cod_limit >= :amount " +
                        "AND dp.status IN ('ONLINE', 'AVAILABLE') " +
                        "AND dp.user_id NOT IN :excludedDriverIds " +
                        "AND w.balance >= 0 " +
                        "ORDER BY dp.average_rating DESC LIMIT 1", nativeQuery = true)
        Optional<DriverProfile> findFirstAvailableDriverByCodLimitExcluding(@Param("amount") BigDecimal amount,
                        @Param("excludedDriverIds") List<Long> excludedDriverIds);

        @Query(value = "SELECT dp.* FROM driver_profiles dp " +
                        "INNER JOIN users u ON dp.user_id = u.id " +
                        "INNER JOIN wallets w ON u.id = w.user_id " +
                        "WHERE dp.status IN ('ONLINE', 'AVAILABLE') " +
                        "AND w.balance >= 0 " +
                        "ORDER BY dp.average_rating DESC LIMIT 1", nativeQuery = true)
        Optional<DriverProfile> findFirstAvailableDriver();

        @Query(value = "SELECT dp.* FROM driver_profiles dp " +
                        "INNER JOIN users u ON dp.user_id = u.id " +
                        "INNER JOIN wallets w ON u.id = w.user_id " +
                        "WHERE dp.status IN ('ONLINE', 'AVAILABLE') " +
                        "AND dp.user_id NOT IN :excludedDriverIds " +
                        "AND w.balance >= 0 " +
                        "ORDER BY dp.average_rating DESC LIMIT 1", nativeQuery = true)
        Optional<DriverProfile> findFirstAvailableDriverExcluding(
                        @Param("excludedDriverIds") List<Long> excludedDriverIds);
}
