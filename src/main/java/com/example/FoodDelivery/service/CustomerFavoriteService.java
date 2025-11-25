package com.example.FoodDelivery.service;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.example.FoodDelivery.domain.CustomerFavorite;
import com.example.FoodDelivery.domain.User;
import com.example.FoodDelivery.domain.res.ResultPaginationDTO;
import com.example.FoodDelivery.repository.CustomerFavoriteRepository;
import com.example.FoodDelivery.repository.UserRepository;
import com.example.FoodDelivery.util.error.IdInvalidException;

@Service
public class CustomerFavoriteService {
    private final CustomerFavoriteRepository customerFavoriteRepository;
    private final UserRepository userRepository;

    public CustomerFavoriteService(CustomerFavoriteRepository customerFavoriteRepository,
            UserRepository userRepository) {
        this.customerFavoriteRepository = customerFavoriteRepository;
        this.userRepository = userRepository;
    }

    public CustomerFavorite getCustomerFavoriteById(Long id) {
        Optional<CustomerFavorite> customerFavoriteOpt = this.customerFavoriteRepository.findById(id);
        return customerFavoriteOpt.orElse(null);
    }

    public List<CustomerFavorite> getCustomerFavoritesByCustomerId(Long customerId) {
        return this.customerFavoriteRepository.findByCustomerId(customerId);
    }

    public CustomerFavorite createCustomerFavorite(CustomerFavorite customerFavorite) throws IdInvalidException {
        // check customer exists
        if (customerFavorite.getCustomer() != null) {
            User customer = this.userRepository.findById(customerFavorite.getCustomer().getId()).orElse(null);
            if (customer == null) {
                throw new IdInvalidException("Customer not found with id: " + customerFavorite.getCustomer().getId());
            }
            customerFavorite.setCustomer(customer);
        } else {
            throw new IdInvalidException("Customer is required");
        }

        // check if already favorited
        boolean exists = this.customerFavoriteRepository.existsByCustomerIdAndTargetTypeAndTargetId(
                customerFavorite.getCustomer().getId(),
                customerFavorite.getTargetType(),
                customerFavorite.getTargetId());
        if (exists) {
            throw new IdInvalidException("This item is already in favorites");
        }

        return customerFavoriteRepository.save(customerFavorite);
    }

    public CustomerFavorite updateCustomerFavorite(CustomerFavorite customerFavorite) throws IdInvalidException {
        // check id
        CustomerFavorite currentCustomerFavorite = getCustomerFavoriteById(customerFavorite.getId());
        if (currentCustomerFavorite == null) {
            throw new IdInvalidException("Customer favorite not found with id: " + customerFavorite.getId());
        }

        if (customerFavorite.getTargetType() != null) {
            currentCustomerFavorite.setTargetType(customerFavorite.getTargetType());
        }
        if (customerFavorite.getTargetId() != null) {
            currentCustomerFavorite.setTargetId(customerFavorite.getTargetId());
        }
        if (customerFavorite.getCustomer() != null) {
            User customer = this.userRepository.findById(customerFavorite.getCustomer().getId()).orElse(null);
            if (customer == null) {
                throw new IdInvalidException("Customer not found with id: " + customerFavorite.getCustomer().getId());
            }
            currentCustomerFavorite.setCustomer(customer);
        }

        return customerFavoriteRepository.save(currentCustomerFavorite);
    }

    public ResultPaginationDTO getAllCustomerFavorites(Specification<CustomerFavorite> spec, Pageable pageable) {
        Page<CustomerFavorite> page = this.customerFavoriteRepository.findAll(spec, pageable);
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

    public void deleteCustomerFavorite(Long id) {
        this.customerFavoriteRepository.deleteById(id);
    }
}
