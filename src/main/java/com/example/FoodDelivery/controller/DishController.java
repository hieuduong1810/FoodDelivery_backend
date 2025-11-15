package com.example.FoodDelivery.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.turkraft.springfilter.boot.Filter;

import com.example.FoodDelivery.domain.Dish;
import com.example.FoodDelivery.domain.res.ResultPaginationDTO;
import com.example.FoodDelivery.service.DishService;
import com.example.FoodDelivery.util.annotation.ApiMessage;
import com.example.FoodDelivery.util.error.IdInvalidException;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class DishController {
    private final DishService dishService;

    public DishController(DishService dishService) {
        this.dishService = dishService;
    }

    @PostMapping("/dishes")
    @ApiMessage("Create dish")
    public ResponseEntity<Dish> createDish(@RequestBody Dish dish) throws IdInvalidException {
        Dish createdDish = dishService.createDish(dish);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdDish);
    }

    @PutMapping("/dishes")
    @ApiMessage("Update dish")
    public ResponseEntity<Dish> updateDish(@RequestBody Dish dish) throws IdInvalidException {
        Dish updatedDish = dishService.updateDish(dish);
        return ResponseEntity.ok(updatedDish);
    }

    @GetMapping("/dishes")
    @ApiMessage("Get all dishes")
    public ResponseEntity<ResultPaginationDTO> getAllDishes(
            @Filter Specification<Dish> spec, Pageable pageable) {
        ResultPaginationDTO result = dishService.getAllDishes(spec, pageable);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/dishes/{id}")
    @ApiMessage("Get dish by id")
    public ResponseEntity<Dish> getDishById(@PathVariable("id") Long id) throws IdInvalidException {
        Dish dish = dishService.getDishById(id);
        if (dish == null) {
            throw new IdInvalidException("Dish not found with id: " + id);
        }
        return ResponseEntity.ok(dish);
    }

    @GetMapping("/dishes/restaurant/{restaurantId}")
    @ApiMessage("Get dishes by restaurant id")
    public ResponseEntity<List<Dish>> getDishesByRestaurantId(@PathVariable("restaurantId") Long restaurantId) {
        List<Dish> dishes = dishService.getDishesByRestaurantId(restaurantId);
        return ResponseEntity.ok(dishes);
    }

    @GetMapping("/dishes/category/{categoryId}")
    @ApiMessage("Get dishes by category id")
    public ResponseEntity<List<Dish>> getDishesByCategoryId(@PathVariable("categoryId") Long categoryId) {
        List<Dish> dishes = dishService.getDishesByCategoryId(categoryId);
        return ResponseEntity.ok(dishes);
    }

    @DeleteMapping("/dishes/{id}")
    @ApiMessage("Delete dish by id")
    public ResponseEntity<Void> deleteDish(@PathVariable("id") Long id) throws IdInvalidException {
        Dish dish = dishService.getDishById(id);
        if (dish == null) {
            throw new IdInvalidException("Dish not found with id: " + id);
        }
        dishService.deleteDish(id);
        return ResponseEntity.ok().body(null);
    }
}
