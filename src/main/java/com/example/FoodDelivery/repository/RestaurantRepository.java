package com.example.FoodDelivery.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.example.FoodDelivery.domain.Restaurant;

import java.util.Optional;

@Repository
public interface RestaurantRepository extends JpaRepository<Restaurant, Long>, JpaSpecificationExecutor<Restaurant> {
    boolean existsByName(String name);

    boolean existsBySlug(String slug);

    Optional<Restaurant> findBySlug(String slug);

    Optional<Restaurant> findByOwnerId(Long ownerId);

    Optional<Restaurant> findByName(String name);
}
