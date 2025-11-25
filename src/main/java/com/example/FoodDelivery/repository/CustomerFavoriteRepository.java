package com.example.FoodDelivery.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.example.FoodDelivery.domain.CustomerFavorite;

import java.util.List;
import java.util.Optional;

@Repository
public interface CustomerFavoriteRepository
        extends JpaRepository<CustomerFavorite, Long>, JpaSpecificationExecutor<CustomerFavorite> {
    List<CustomerFavorite> findByCustomerId(Long customerId);

    Optional<CustomerFavorite> findByCustomerIdAndTargetTypeAndTargetId(Long customerId, String targetType,
            Long targetId);

    boolean existsByCustomerIdAndTargetTypeAndTargetId(Long customerId, String targetType, Long targetId);
}
