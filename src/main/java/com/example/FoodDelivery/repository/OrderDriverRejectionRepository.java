package com.example.FoodDelivery.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.FoodDelivery.domain.OrderDriverRejection;

import java.util.List;

@Repository
public interface OrderDriverRejectionRepository extends JpaRepository<OrderDriverRejection, Long> {

    @Query("SELECT odr.driver.id FROM OrderDriverRejection odr WHERE odr.order.id = :orderId")
    List<Long> findRejectedDriverIdsByOrderId(@Param("orderId") Long orderId);
}
