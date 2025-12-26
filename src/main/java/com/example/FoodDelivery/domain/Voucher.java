package com.example.FoodDelivery.domain;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "vouchers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Voucher {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String code;

    @Column(columnDefinition = "TEXT")
    private String description;

    private String discountType;

    @Column(precision = 10, scale = 2)
    private BigDecimal discountValue;

    @Column(precision = 10, scale = 2)
    private BigDecimal minOrderValue;

    @Column(precision = 10, scale = 2)
    private BigDecimal maxDiscountAmount;

    private Integer usageLimitPerUser;
    private Instant startDate;
    private Instant endDate;
    private Integer totalQuantity;

    @ManyToOne
    @JoinColumn(name = "restaurant_id")
    private Restaurant restaurant;

    @OneToMany(mappedBy = "voucher", fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Order> orders;
}