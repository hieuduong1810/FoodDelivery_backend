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

        // Find available drivers with COD limit >= amount, within radius (using
        // Haversine for initial filter)
        // Returns list to be sorted by real driving distance using Mapbox API
        @Query(value = "SELECT dp.* FROM driver_profiles dp " +
                        "INNER JOIN users u ON dp.user_id = u.id " +
                        "INNER JOIN wallets w ON u.id = w.user_id " +
                        "WHERE dp.cod_limit >= :amount " +
                        "AND dp.status IN ('ONLINE', 'AVAILABLE') " +
                        "AND w.balance >= 0 " +
                        "AND dp.current_latitude IS NOT NULL " +
                        "AND dp.current_longitude IS NOT NULL " +
                        "AND (6371 * acos(cos(radians(:restaurantLat)) * cos(radians(dp.current_latitude)) * " +
                        "cos(radians(dp.current_longitude) - radians(:restaurantLng)) + " +
                        "sin(radians(:restaurantLat)) * sin(radians(dp.current_latitude)))) <= :radiusKm", nativeQuery = true)
        List<DriverProfile> findAvailableDriversByCodLimitWithinRadius(
                        @Param("amount") BigDecimal amount,
                        @Param("restaurantLat") BigDecimal restaurantLat,
                        @Param("restaurantLng") BigDecimal restaurantLng,
                        @Param("radiusKm") BigDecimal radiusKm);

        // Find available drivers excluding specific driver IDs, within radius
        @Query(value = "SELECT dp.* FROM driver_profiles dp " +
                        "INNER JOIN users u ON dp.user_id = u.id " +
                        "INNER JOIN wallets w ON u.id = w.user_id " +
                        "WHERE dp.cod_limit >= :amount " +
                        "AND dp.status IN ('ONLINE', 'AVAILABLE') " +
                        "AND dp.user_id NOT IN :excludedDriverIds " +
                        "AND w.balance >= 0 " +
                        "AND dp.current_latitude IS NOT NULL " +
                        "AND dp.current_longitude IS NOT NULL " +
                        "AND (6371 * acos(cos(radians(:restaurantLat)) * cos(radians(dp.current_latitude)) * " +
                        "cos(radians(dp.current_longitude) - radians(:restaurantLng)) + " +
                        "sin(radians(:restaurantLat)) * sin(radians(dp.current_latitude)))) <= :radiusKm", nativeQuery = true)
        List<DriverProfile> findAvailableDriversByCodLimitExcludingWithinRadius(
                        @Param("amount") BigDecimal amount,
                        @Param("restaurantLat") BigDecimal restaurantLat,
                        @Param("restaurantLng") BigDecimal restaurantLng,
                        @Param("radiusKm") BigDecimal radiusKm,
                        @Param("excludedDriverIds") List<Long> excludedDriverIds);

        @Query(value = "SELECT dp.* FROM driver_profiles dp " +
                        "INNER JOIN users u ON dp.user_id = u.id " +
                        "INNER JOIN wallets w ON u.id = w.user_id " +
                        "WHERE dp.status IN ('ONLINE', 'AVAILABLE') " +
                        "AND w.balance >= 0 " +
                        "AND dp.current_latitude IS NOT NULL " +
                        "AND dp.current_longitude IS NOT NULL " +
                        "AND (6371 * acos(cos(radians(:restaurantLat)) * cos(radians(dp.current_latitude)) * " +
                        "cos(radians(dp.current_longitude) - radians(:restaurantLng)) + " +
                        "sin(radians(:restaurantLat)) * sin(radians(dp.current_latitude)))) <= :radiusKm", nativeQuery = true)
        List<DriverProfile> findAvailableDriversWithinRadius(
                        @Param("restaurantLat") BigDecimal restaurantLat,
                        @Param("restaurantLng") BigDecimal restaurantLng,
                        @Param("radiusKm") BigDecimal radiusKm);

        @Query(value = "SELECT dp.* FROM driver_profiles dp " +
                        "INNER JOIN users u ON dp.user_id = u.id " +
                        "INNER JOIN wallets w ON u.id = w.user_id " +
                        "WHERE dp.status IN ('ONLINE', 'AVAILABLE') " +
                        "AND dp.user_id NOT IN :excludedDriverIds " +
                        "AND w.balance >= 0 " +
                        "AND dp.current_latitude IS NOT NULL " +
                        "AND dp.current_longitude IS NOT NULL " +
                        "AND (6371 * acos(cos(radians(:restaurantLat)) * cos(radians(dp.current_latitude)) * " +
                        "cos(radians(dp.current_longitude) - radians(:restaurantLng)) + " +
                        "sin(radians(:restaurantLat)) * sin(radians(dp.current_latitude)))) <= :radiusKm", nativeQuery = true)
        List<DriverProfile> findAvailableDriversExcludingWithinRadius(
                        @Param("restaurantLat") BigDecimal restaurantLat,
                        @Param("restaurantLng") BigDecimal restaurantLng,
                        @Param("radiusKm") BigDecimal radiusKm,
                        @Param("excludedDriverIds") List<Long> excludedDriverIds);
}
