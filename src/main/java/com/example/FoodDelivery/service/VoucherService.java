package com.example.FoodDelivery.service;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.example.FoodDelivery.domain.Voucher;
import com.example.FoodDelivery.domain.Restaurant;
import com.example.FoodDelivery.domain.res.ResultPaginationDTO;
import com.example.FoodDelivery.repository.VoucherRepository;
import com.example.FoodDelivery.repository.RestaurantRepository;
import com.example.FoodDelivery.util.error.IdInvalidException;

@Service
public class VoucherService {
    private final VoucherRepository voucherRepository;
    private final RestaurantRepository restaurantRepository;

    public VoucherService(VoucherRepository voucherRepository, RestaurantRepository restaurantRepository) {
        this.voucherRepository = voucherRepository;
        this.restaurantRepository = restaurantRepository;
    }

    public Voucher getVoucherById(Long id) {
        Optional<Voucher> voucherOpt = this.voucherRepository.findById(id);
        return voucherOpt.orElse(null);
    }

    public Voucher getVoucherByCode(String code) {
        Optional<Voucher> voucherOpt = this.voucherRepository.findByCode(code);
        return voucherOpt.orElse(null);
    }

    public List<Voucher> getVouchersByRestaurantId(Long restaurantId) {
        return this.voucherRepository.findByRestaurantId(restaurantId);
    }

    public Voucher createVoucher(Voucher voucher) throws IdInvalidException {
        // check code exists
        if (this.voucherRepository.existsByCode(voucher.getCode())) {
            throw new IdInvalidException("Voucher code already exists: " + voucher.getCode());
        }

        // check restaurant exists if provided
        if (voucher.getRestaurant() != null) {
            Restaurant restaurant = this.restaurantRepository.findById(voucher.getRestaurant().getId()).orElse(null);
            if (restaurant == null) {
                throw new IdInvalidException("Restaurant not found with id: " + voucher.getRestaurant().getId());
            }
            voucher.setRestaurant(restaurant);
        }

        return voucherRepository.save(voucher);
    }

    public Voucher updateVoucher(Voucher voucher) throws IdInvalidException {
        // check id
        Voucher currentVoucher = getVoucherById(voucher.getId());
        if (currentVoucher == null) {
            throw new IdInvalidException("Voucher not found with id: " + voucher.getId());
        }

        if (voucher.getCode() != null && !voucher.getCode().equals(currentVoucher.getCode())) {
            if (this.voucherRepository.existsByCode(voucher.getCode())) {
                throw new IdInvalidException("Voucher code already exists: " + voucher.getCode());
            }
            currentVoucher.setCode(voucher.getCode());
        }
        if (voucher.getDescription() != null) {
            currentVoucher.setDescription(voucher.getDescription());
        }
        if (voucher.getDiscountType() != null) {
            currentVoucher.setDiscountType(voucher.getDiscountType());
        }
        if (voucher.getDiscountValue() != null) {
            currentVoucher.setDiscountValue(voucher.getDiscountValue());
        }
        if (voucher.getMinOrderValue() != null) {
            currentVoucher.setMinOrderValue(voucher.getMinOrderValue());
        }
        if (voucher.getStartDate() != null) {
            currentVoucher.setStartDate(voucher.getStartDate());
        }
        if (voucher.getEndDate() != null) {
            currentVoucher.setEndDate(voucher.getEndDate());
        }
        if (voucher.getTotalQuantity() != null) {
            currentVoucher.setTotalQuantity(voucher.getTotalQuantity());
        }
        if (voucher.getCreatorType() != null) {
            currentVoucher.setCreatorType(voucher.getCreatorType());
        }
        if (voucher.getRestaurant() != null) {
            Restaurant restaurant = this.restaurantRepository.findById(voucher.getRestaurant().getId()).orElse(null);
            if (restaurant == null) {
                throw new IdInvalidException("Restaurant not found with id: " + voucher.getRestaurant().getId());
            }
            currentVoucher.setRestaurant(restaurant);
        }

        return voucherRepository.save(currentVoucher);
    }

    public ResultPaginationDTO getAllVouchers(Specification<Voucher> spec, Pageable pageable) {
        Page<Voucher> page = this.voucherRepository.findAll(spec, pageable);
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

    public void deleteVoucher(Long id) {
        this.voucherRepository.deleteById(id);
    }
}
