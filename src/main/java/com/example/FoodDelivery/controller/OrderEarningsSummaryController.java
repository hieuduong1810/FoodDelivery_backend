package com.example.FoodDelivery.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.turkraft.springfilter.boot.Filter;

import com.example.FoodDelivery.domain.OrderEarningsSummary;
import com.example.FoodDelivery.domain.res.ResultPaginationDTO;
import com.example.FoodDelivery.service.OrderEarningsSummaryService;
import com.example.FoodDelivery.util.annotation.ApiMessage;
import com.example.FoodDelivery.util.error.IdInvalidException;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class OrderEarningsSummaryController {
    private final OrderEarningsSummaryService orderEarningsSummaryService;

    public OrderEarningsSummaryController(OrderEarningsSummaryService orderEarningsSummaryService) {
        this.orderEarningsSummaryService = orderEarningsSummaryService;
    }

    @PostMapping("/order-earnings-summaries")
    @ApiMessage("Create order earnings summary")
    public ResponseEntity<OrderEarningsSummary> createOrderEarningsSummary(
            @RequestBody OrderEarningsSummary orderEarningsSummary) throws IdInvalidException {
        OrderEarningsSummary createdSummary = orderEarningsSummaryService
                .createOrderEarningsSummary(orderEarningsSummary);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdSummary);
    }

    @PostMapping("/order-earnings-summaries/generate/{orderId}")
    @ApiMessage("Generate order earnings summary from order")
    public ResponseEntity<OrderEarningsSummary> generateOrderEarningsSummary(@PathVariable("orderId") Long orderId)
            throws IdInvalidException {
        OrderEarningsSummary summary = orderEarningsSummaryService.createOrderEarningsSummaryFromOrder(orderId);
        return ResponseEntity.status(HttpStatus.CREATED).body(summary);
    }

    @PutMapping("/order-earnings-summaries")
    @ApiMessage("Update order earnings summary")
    public ResponseEntity<OrderEarningsSummary> updateOrderEarningsSummary(
            @RequestBody OrderEarningsSummary orderEarningsSummary) throws IdInvalidException {
        OrderEarningsSummary updatedSummary = orderEarningsSummaryService
                .updateOrderEarningsSummary(orderEarningsSummary);
        return ResponseEntity.ok(updatedSummary);
    }

    @GetMapping("/order-earnings-summaries")
    @ApiMessage("Get all order earnings summaries")
    public ResponseEntity<ResultPaginationDTO> getAllOrderEarningsSummaries(
            @Filter Specification<OrderEarningsSummary> spec, Pageable pageable) {
        ResultPaginationDTO result = orderEarningsSummaryService.getAllOrderEarningsSummaries(spec, pageable);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/order-earnings-summaries/{id}")
    @ApiMessage("Get order earnings summary by id")
    public ResponseEntity<OrderEarningsSummary> getOrderEarningsSummaryById(@PathVariable("id") Long id)
            throws IdInvalidException {
        OrderEarningsSummary summary = orderEarningsSummaryService.getOrderEarningsSummaryById(id);
        if (summary == null) {
            throw new IdInvalidException("Order earnings summary not found with id: " + id);
        }
        return ResponseEntity.ok(summary);
    }

    @GetMapping("/order-earnings-summaries/order/{orderId}")
    @ApiMessage("Get order earnings summary by order id")
    public ResponseEntity<OrderEarningsSummary> getOrderEarningsSummaryByOrderId(@PathVariable("orderId") Long orderId)
            throws IdInvalidException {
        OrderEarningsSummary summary = orderEarningsSummaryService.getOrderEarningsSummaryByOrderId(orderId);
        if (summary == null) {
            throw new IdInvalidException("Order earnings summary not found for order id: " + orderId);
        }
        return ResponseEntity.ok(summary);
    }

    @GetMapping("/order-earnings-summaries/driver/{driverId}")
    @ApiMessage("Get order earnings summaries by driver id")
    public ResponseEntity<List<OrderEarningsSummary>> getOrderEarningsSummariesByDriverId(
            @PathVariable("driverId") Long driverId) {
        List<OrderEarningsSummary> summaries = orderEarningsSummaryService
                .getOrderEarningsSummariesByDriverId(driverId);
        return ResponseEntity.ok(summaries);
    }

    @GetMapping("/order-earnings-summaries/restaurant/{restaurantId}")
    @ApiMessage("Get order earnings summaries by restaurant id")
    public ResponseEntity<List<OrderEarningsSummary>> getOrderEarningsSummariesByRestaurantId(
            @PathVariable("restaurantId") Long restaurantId) {
        List<OrderEarningsSummary> summaries = orderEarningsSummaryService
                .getOrderEarningsSummariesByRestaurantId(restaurantId);
        return ResponseEntity.ok(summaries);
    }

    @GetMapping("/order-earnings-summaries/date-range")
    @ApiMessage("Get order earnings summaries by date range")
    public ResponseEntity<List<OrderEarningsSummary>> getOrderEarningsSummariesByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant endDate) {
        List<OrderEarningsSummary> summaries = orderEarningsSummaryService
                .getOrderEarningsSummariesByDateRange(startDate, endDate);
        return ResponseEntity.ok(summaries);
    }

    @GetMapping("/order-earnings-summaries/driver/{driverId}/total")
    @ApiMessage("Get total driver earnings")
    public ResponseEntity<BigDecimal> getTotalDriverEarnings(@PathVariable("driverId") Long driverId) {
        BigDecimal total = orderEarningsSummaryService.getTotalDriverEarnings(driverId);
        return ResponseEntity.ok(total);
    }

    @GetMapping("/order-earnings-summaries/restaurant/{restaurantId}/total")
    @ApiMessage("Get total restaurant earnings")
    public ResponseEntity<BigDecimal> getTotalRestaurantEarnings(@PathVariable("restaurantId") Long restaurantId) {
        BigDecimal total = orderEarningsSummaryService.getTotalRestaurantEarnings(restaurantId);
        return ResponseEntity.ok(total);
    }

    @GetMapping("/order-earnings-summaries/platform/total")
    @ApiMessage("Get total platform earnings")
    public ResponseEntity<BigDecimal> getTotalPlatformEarnings(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant endDate) {
        BigDecimal total = orderEarningsSummaryService.getTotalPlatformEarnings(startDate, endDate);
        return ResponseEntity.ok(total);
    }

    @DeleteMapping("/order-earnings-summaries/{id}")
    @ApiMessage("Delete order earnings summary by id")
    public ResponseEntity<Void> deleteOrderEarningsSummary(@PathVariable("id") Long id) throws IdInvalidException {
        OrderEarningsSummary summary = orderEarningsSummaryService.getOrderEarningsSummaryById(id);
        if (summary == null) {
            throw new IdInvalidException("Order earnings summary not found with id: " + id);
        }
        orderEarningsSummaryService.deleteOrderEarningsSummary(id);
        return ResponseEntity.ok().body(null);
    }
}
