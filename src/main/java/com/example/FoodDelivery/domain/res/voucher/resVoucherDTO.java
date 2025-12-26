package com.example.FoodDelivery.domain.res.voucher;

import java.math.BigDecimal;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class resVoucherDTO {
    private Long id;
    private String code;
    private String description;
    private String discountType;
    private BigDecimal discountValue;
    private BigDecimal minOrderValue;
    private BigDecimal maxDiscountAmount;
    private Integer usageLimitPerUser;
    private Instant startDate;
    private Instant endDate;
    private Integer totalQuantity;
    private String creatorType;
    private Restaurant restaurant;

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Restaurant {
        private long id;
        private String name;
    }
}
