package com.example.FoodDelivery.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.example.FoodDelivery.domain.OrderEarningsSummary;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderEarningsSummaryRepository extends JpaRepository<OrderEarningsSummary, Long>, JpaSpecificationExecutor<OrderEarningsSummary> {
    Optional<OrderEarningsSummary> findByOrderId(Long orderId);
    List<OrderEarningsSummary> findByDriverId(Long driverId);
    List<OrderEarningsSummary> findByRestaurantId(Long restaurantId);
    List<OrderEarningsSummary> findByRecordedAtBetween(Instant startDate, Instant endDate);
    
    @Query("SELECT SUM(o.driverNetEarning) FROM OrderEarningsSummary o WHERE o.driver.id = ?1")
    BigDecimal sumDriverEarnings(Long driverId);
    
    @Query("SELECT SUM(o.restaurantNetEarning) FROM OrderEarningsSummary o WHERE o.restaurant.id = ?1")
    BigDecimal sumRestaurantEarnings(Long restaurantId);
    
    @Query("SELECT SUM(o.platformTotalEarning) FROM OrderEarningsSummary o WHERE o.recordedAt BETWEEN ?1 AND ?2")
    BigDecimal sumPlatformEarnings(Instant startDate, Instant endDate);
}
