package com.example.FoodDelivery.domain.res.review;

import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ResReviewDTO {
    private Long id;
    private Order order;
    private User customer;
    private String reviewTarget;
    private String targetName;
    private Integer rating;
    private String comment;
    private String reply;
    private Instant createdAt;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Order {
        private Long id;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class User {
        private Long id;
        private String name;
    }
}
