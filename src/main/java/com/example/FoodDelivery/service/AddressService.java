package com.example.FoodDelivery.service;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.example.FoodDelivery.domain.Address;
import com.example.FoodDelivery.domain.User;
import com.example.FoodDelivery.domain.res.ResultPaginationDTO;
import com.example.FoodDelivery.repository.AddressRepository;
import com.example.FoodDelivery.util.error.IdInvalidException;

@Service
public class AddressService {
    private final AddressRepository addressRepository;
    private final UserService userService;

    public AddressService(AddressRepository addressRepository, UserService userService) {
        this.addressRepository = addressRepository;
        this.userService = userService;
    }

    public Address getAddressById(Long id) {
        Optional<Address> addressOpt = this.addressRepository.findById(id);
        return addressOpt.orElse(null);
    }

    public List<Address> getAddressesByCustomerId(Long customerId) {
        return this.addressRepository.findByCustomerId(customerId);
    }

    public Address createAddress(Address address) throws IdInvalidException {
        // check customer exists
        if (address.getCustomer() != null) {
            User customer = this.userService.getUserById(address.getCustomer().getId());
            if (customer == null) {
                throw new IdInvalidException("Customer not found with id: " + address.getCustomer().getId());
            }
            address.setCustomer(customer);
        } else {
            throw new IdInvalidException("Customer is required");
        }

        return addressRepository.save(address);
    }

    public Address updateAddress(Address address) throws IdInvalidException {
        // check id
        Address currentAddress = getAddressById(address.getId());
        if (currentAddress == null) {
            throw new IdInvalidException("Address not found with id: " + address.getId());
        }

        // update fields
        if (address.getAddressLine() != null) {
            currentAddress.setAddressLine(address.getAddressLine());
        }
        if (address.getLatitude() != null) {
            currentAddress.setLatitude(address.getLatitude());
        }
        if (address.getLongitude() != null) {
            currentAddress.setLongitude(address.getLongitude());
        }
        if (address.getLabel() != null) {
            currentAddress.setLabel(address.getLabel());
        }
        if (address.getCustomer() != null) {
            User customer = this.userService.getUserById(address.getCustomer().getId());
            if (customer == null) {
                throw new IdInvalidException("Customer not found with id: " + address.getCustomer().getId());
            }
            currentAddress.setCustomer(customer);
        }

        return addressRepository.save(currentAddress);
    }

    public ResultPaginationDTO getAllAddresses(Specification<Address> spec, Pageable pageable) {
        Page<Address> page = this.addressRepository.findAll(spec, pageable);
        ResultPaginationDTO result = new ResultPaginationDTO();
        ResultPaginationDTO.Meta meta = new ResultPaginationDTO.Meta();
        meta.setPage(pageable.getPageNumber() + 1);
        meta.setPageSize(pageable.getPageSize());
        meta.setTotal(page.getTotalElements());
        meta.setPages(page.getTotalPages());
        result.setMeta(meta);
        result.setResult(page.getContent());
        return result;
    }

    public void deleteAddress(Long id) {
        this.addressRepository.deleteById(id);
    }
}
