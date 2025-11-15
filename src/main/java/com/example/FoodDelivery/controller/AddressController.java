package com.example.FoodDelivery.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.turkraft.springfilter.boot.Filter;

import com.example.FoodDelivery.domain.Address;
import com.example.FoodDelivery.domain.res.ResultPaginationDTO;
import com.example.FoodDelivery.service.AddressService;
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
public class AddressController {
    private final AddressService addressService;

    public AddressController(AddressService addressService) {
        this.addressService = addressService;
    }

    @PostMapping("/addresses")
    @ApiMessage("Create address")
    public ResponseEntity<Address> createAddress(@RequestBody Address address) throws IdInvalidException {
        Address createdAddress = addressService.createAddress(address);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdAddress);
    }

    @PutMapping("/addresses")
    @ApiMessage("Update address")
    public ResponseEntity<Address> updateAddress(@RequestBody Address address) throws IdInvalidException {
        Address updatedAddress = addressService.updateAddress(address);
        return ResponseEntity.ok(updatedAddress);
    }

    @GetMapping("/addresses")
    @ApiMessage("Get all addresses")
    public ResponseEntity<ResultPaginationDTO> getAllAddresses(
            @Filter Specification<Address> spec, Pageable pageable) {
        ResultPaginationDTO result = addressService.getAllAddresses(spec, pageable);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/addresses/{id}")
    @ApiMessage("Get address by id")
    public ResponseEntity<Address> getAddressById(@PathVariable("id") Long id) throws IdInvalidException {
        Address address = addressService.getAddressById(id);
        if (address == null) {
            throw new IdInvalidException("Address not found with id: " + id);
        }
        return ResponseEntity.ok(address);
    }

    @GetMapping("/addresses/customer/{customerId}")
    @ApiMessage("Get addresses by customer id")
    public ResponseEntity<List<Address>> getAddressesByCustomerId(@PathVariable("customerId") Long customerId) {
        List<Address> addresses = addressService.getAddressesByCustomerId(customerId);
        return ResponseEntity.ok(addresses);
    }

    @DeleteMapping("/addresses/{id}")
    @ApiMessage("Delete address by id")
    public ResponseEntity<Void> deleteAddress(@PathVariable("id") Long id) throws IdInvalidException {
        Address address = addressService.getAddressById(id);
        if (address == null) {
            throw new IdInvalidException("Address not found with id: " + id);
        }
        addressService.deleteAddress(id);
        return ResponseEntity.ok().body(null);
    }
}
