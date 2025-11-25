package com.example.FoodDelivery.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.example.FoodDelivery.domain.MenuOptionGroup;

import java.util.List;

@Repository
public interface MenuOptionGroupRepository
        extends JpaRepository<MenuOptionGroup, Long>, JpaSpecificationExecutor<MenuOptionGroup> {
    List<MenuOptionGroup> findByDishId(Long dishId);
}
