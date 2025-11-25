package com.example.FoodDelivery.service;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.example.FoodDelivery.domain.OrderItemOption;
import com.example.FoodDelivery.domain.OrderItem;
import com.example.FoodDelivery.domain.res.ResultPaginationDTO;
import com.example.FoodDelivery.repository.OrderItemOptionRepository;
import com.example.FoodDelivery.repository.OrderItemRepository;
import com.example.FoodDelivery.util.error.IdInvalidException;

@Service
public class OrderItemOptionService {
    private final OrderItemOptionRepository orderItemOptionRepository;
    private final OrderItemRepository orderItemRepository;

    public OrderItemOptionService(OrderItemOptionRepository orderItemOptionRepository,
            OrderItemRepository orderItemRepository) {
        this.orderItemOptionRepository = orderItemOptionRepository;
        this.orderItemRepository = orderItemRepository;
    }

    public OrderItemOption getOrderItemOptionById(Long id) {
        Optional<OrderItemOption> orderItemOptionOpt = this.orderItemOptionRepository.findById(id);
        return orderItemOptionOpt.orElse(null);
    }

    public List<OrderItemOption> getOrderItemOptionsByOrderItemId(Long orderItemId) {
        return this.orderItemOptionRepository.findByOrderItemId(orderItemId);
    }

    public OrderItemOption createOrderItemOption(OrderItemOption orderItemOption) throws IdInvalidException {
        // check order item exists
        if (orderItemOption.getOrderItem() != null) {
            OrderItem orderItem = this.orderItemRepository.findById(orderItemOption.getOrderItem().getId())
                    .orElse(null);
            if (orderItem == null) {
                throw new IdInvalidException("Order item not found with id: " + orderItemOption.getOrderItem().getId());
            }
            orderItemOption.setOrderItem(orderItem);
        } else {
            throw new IdInvalidException("Order item is required");
        }

        return orderItemOptionRepository.save(orderItemOption);
    }

    public OrderItemOption updateOrderItemOption(OrderItemOption orderItemOption) throws IdInvalidException {
        // check id
        OrderItemOption currentOrderItemOption = getOrderItemOptionById(orderItemOption.getOptionId());
        if (currentOrderItemOption == null) {
            throw new IdInvalidException("Order item option not found with id: " + orderItemOption.getOptionId());
        }

        if (orderItemOption.getOptionName() != null) {
            currentOrderItemOption.setOptionName(orderItemOption.getOptionName());
        }
        if (orderItemOption.getPriceAtPurchase() != null) {
            currentOrderItemOption.setPriceAtPurchase(orderItemOption.getPriceAtPurchase());
        }
        if (orderItemOption.getOrderItem() != null) {
            OrderItem orderItem = this.orderItemRepository.findById(orderItemOption.getOrderItem().getId())
                    .orElse(null);
            if (orderItem == null) {
                throw new IdInvalidException("Order item not found with id: " + orderItemOption.getOrderItem().getId());
            }
            currentOrderItemOption.setOrderItem(orderItem);
        }

        return orderItemOptionRepository.save(currentOrderItemOption);
    }

    public ResultPaginationDTO getAllOrderItemOptions(Specification<OrderItemOption> spec, Pageable pageable) {
        Page<OrderItemOption> page = this.orderItemOptionRepository.findAll(spec, pageable);
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

    public void deleteOrderItemOption(Long id) {
        this.orderItemOptionRepository.deleteById(id);
    }
}
