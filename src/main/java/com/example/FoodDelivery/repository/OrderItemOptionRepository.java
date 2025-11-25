package com.example.FoodDelivery.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.example.FoodDelivery.domain.OrderItemOption;

import java.util.List;

@Repository
public interface OrderItemOptionRepository
        extends JpaRepository<OrderItemOption, Long>, JpaSpecificationExecutor<OrderItemOption> {
    List<OrderItemOption> findByOrderItemId(Long orderItemId);
}
