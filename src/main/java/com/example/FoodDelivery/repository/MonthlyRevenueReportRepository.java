package com.example.FoodDelivery.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.example.FoodDelivery.domain.MonthlyRevenueReport;

import java.util.List;
import java.util.Optional;

@Repository
public interface MonthlyRevenueReportRepository
        extends JpaRepository<MonthlyRevenueReport, Long>, JpaSpecificationExecutor<MonthlyRevenueReport> {
    Optional<MonthlyRevenueReport> findByRestaurantIdAndMonthAndYear(Long restaurantId, Integer month, Integer year);

    List<MonthlyRevenueReport> findByRestaurantId(Long restaurantId);

    List<MonthlyRevenueReport> findByRestaurantIdOrderByYearDescMonthDesc(Long restaurantId);

    List<MonthlyRevenueReport> findByYear(Integer year);

    List<MonthlyRevenueReport> findByMonthAndYear(Integer month, Integer year);

    boolean existsByRestaurantIdAndMonthAndYear(Long restaurantId, Integer month, Integer year);

    @Query("SELECT SUM(m.totalRevenue) FROM MonthlyRevenueReport m WHERE m.restaurant.id = ?1 AND m.year = ?2")
    java.math.BigDecimal sumTotalRevenueByRestaurantAndYear(Long restaurantId, Integer year);
}
