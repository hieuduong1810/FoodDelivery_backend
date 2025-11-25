package com.example.FoodDelivery.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.turkraft.springfilter.boot.Filter;

import jakarta.validation.Valid;
import com.example.FoodDelivery.domain.CustomerFavorite;
import com.example.FoodDelivery.domain.res.ResultPaginationDTO;
import com.example.FoodDelivery.service.CustomerFavoriteService;
import com.example.FoodDelivery.util.annotation.ApiMessage;
import com.example.FoodDelivery.util.error.IdInvalidException;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class CustomerFavoriteController {
    private final CustomerFavoriteService customerFavoriteService;

    public CustomerFavoriteController(CustomerFavoriteService customerFavoriteService) {
        this.customerFavoriteService = customerFavoriteService;
    }

    @PostMapping("/customer-favorites")
    @ApiMessage("Create new customer favorite")
    public ResponseEntity<CustomerFavorite> createCustomerFavorite(
            @Valid @RequestBody CustomerFavorite customerFavorite)
            throws IdInvalidException {
        CustomerFavorite createdCustomerFavorite = customerFavoriteService.createCustomerFavorite(customerFavorite);
        return ResponseEntity.ok(createdCustomerFavorite);
    }

    @PutMapping("/customer-favorites")
    @ApiMessage("Update customer favorite")
    public ResponseEntity<CustomerFavorite> updateCustomerFavorite(@RequestBody CustomerFavorite customerFavorite)
            throws IdInvalidException {
        CustomerFavorite updatedCustomerFavorite = customerFavoriteService.updateCustomerFavorite(customerFavorite);
        return ResponseEntity.ok(updatedCustomerFavorite);
    }

    @GetMapping("/customer-favorites")
    @ApiMessage("Get all customer favorites")
    public ResponseEntity<ResultPaginationDTO> getAllCustomerFavorites(
            @Filter Specification<CustomerFavorite> spec, Pageable pageable) {
        ResultPaginationDTO result = customerFavoriteService.getAllCustomerFavorites(spec, pageable);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/customer-favorites/{id}")
    @ApiMessage("Get customer favorite by id")
    public ResponseEntity<CustomerFavorite> getCustomerFavoriteById(@PathVariable("id") Long id)
            throws IdInvalidException {
        CustomerFavorite customerFavorite = customerFavoriteService.getCustomerFavoriteById(id);
        if (customerFavorite == null) {
            throw new IdInvalidException("Customer favorite not found with id: " + id);
        }
        return ResponseEntity.ok(customerFavorite);
    }

    @GetMapping("/customer-favorites/customer/{customerId}")
    @ApiMessage("Get customer favorites by customer id")
    public ResponseEntity<List<CustomerFavorite>> getCustomerFavoritesByCustomerId(
            @PathVariable("customerId") Long customerId) {
        List<CustomerFavorite> customerFavorites = customerFavoriteService.getCustomerFavoritesByCustomerId(customerId);
        return ResponseEntity.ok(customerFavorites);
    }

    @DeleteMapping("/customer-favorites/{id}")
    @ApiMessage("Delete customer favorite by id")
    public ResponseEntity<Void> deleteCustomerFavorite(@PathVariable("id") Long id) throws IdInvalidException {
        CustomerFavorite customerFavorite = customerFavoriteService.getCustomerFavoriteById(id);
        if (customerFavorite == null) {
            throw new IdInvalidException("Customer favorite not found with id: " + id);
        }
        customerFavoriteService.deleteCustomerFavorite(id);
        return ResponseEntity.ok().body(null);
    }
}
