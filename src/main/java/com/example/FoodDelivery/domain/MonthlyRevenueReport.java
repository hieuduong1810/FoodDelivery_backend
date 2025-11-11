package com.example.FoodDelivery.domain;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "monthly_revenue_reports")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MonthlyRevenueReport {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "restaurant_id")
    private Restaurant restaurant;

    private Integer month;
    private Integer year;

    @Column(precision = 15, scale = 2)
    private BigDecimal totalRevenue;

    @Column(precision = 15, scale = 2)
    private BigDecimal totalCommission;

    @Column(precision = 15, scale = 2)
    private BigDecimal netPayout;

    private Integer totalOrders;

    private Instant generatedAt;

    @PrePersist
    public void handleBeforeCreate() {
        this.generatedAt = Instant.now();
    }
}