package com.example.FoodDelivery.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.turkraft.springfilter.boot.Filter;

import com.example.FoodDelivery.domain.MonthlyRevenueReport;
import com.example.FoodDelivery.domain.res.ResultPaginationDTO;
import com.example.FoodDelivery.service.MonthlyRevenueReportService;
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
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class MonthlyRevenueReportController {
    private final MonthlyRevenueReportService monthlyRevenueReportService;

    public MonthlyRevenueReportController(MonthlyRevenueReportService monthlyRevenueReportService) {
        this.monthlyRevenueReportService = monthlyRevenueReportService;
    }

    @PostMapping("/monthly-revenue-reports")
    @ApiMessage("Create monthly revenue report")
    public ResponseEntity<MonthlyRevenueReport> createMonthlyRevenueReport(
            @RequestBody MonthlyRevenueReport monthlyRevenueReport) throws IdInvalidException {
        MonthlyRevenueReport createdReport = monthlyRevenueReportService
                .createMonthlyRevenueReport(monthlyRevenueReport);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdReport);
    }

    @PostMapping("/monthly-revenue-reports/generate")
    @ApiMessage("Generate monthly revenue report")
    public ResponseEntity<MonthlyRevenueReport> generateMonthlyRevenueReport(
            @RequestParam Long restaurantId,
            @RequestParam Integer month,
            @RequestParam Integer year) throws IdInvalidException {
        MonthlyRevenueReport report = monthlyRevenueReportService.generateMonthlyRevenueReport(restaurantId, month,
                year);
        return ResponseEntity.status(HttpStatus.CREATED).body(report);
    }

    @PostMapping("/monthly-revenue-reports/{id}/regenerate")
    @ApiMessage("Regenerate monthly revenue report")
    public ResponseEntity<MonthlyRevenueReport> regenerateMonthlyRevenueReport(@PathVariable("id") Long id)
            throws IdInvalidException {
        MonthlyRevenueReport report = monthlyRevenueReportService.regenerateMonthlyRevenueReport(id);
        return ResponseEntity.ok(report);
    }

    @PutMapping("/monthly-revenue-reports")
    @ApiMessage("Update monthly revenue report")
    public ResponseEntity<MonthlyRevenueReport> updateMonthlyRevenueReport(
            @RequestBody MonthlyRevenueReport monthlyRevenueReport) throws IdInvalidException {
        MonthlyRevenueReport updatedReport = monthlyRevenueReportService
                .updateMonthlyRevenueReport(monthlyRevenueReport);
        return ResponseEntity.ok(updatedReport);
    }

    @GetMapping("/monthly-revenue-reports")
    @ApiMessage("Get all monthly revenue reports")
    public ResponseEntity<ResultPaginationDTO> getAllMonthlyRevenueReports(
            @Filter Specification<MonthlyRevenueReport> spec, Pageable pageable) {
        ResultPaginationDTO result = monthlyRevenueReportService.getAllMonthlyRevenueReports(spec, pageable);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/monthly-revenue-reports/{id}")
    @ApiMessage("Get monthly revenue report by id")
    public ResponseEntity<MonthlyRevenueReport> getMonthlyRevenueReportById(@PathVariable("id") Long id)
            throws IdInvalidException {
        MonthlyRevenueReport report = monthlyRevenueReportService.getMonthlyRevenueReportById(id);
        if (report == null) {
            throw new IdInvalidException("Monthly revenue report not found with id: " + id);
        }
        return ResponseEntity.ok(report);
    }

    @GetMapping("/monthly-revenue-reports/restaurant/{restaurantId}")
    @ApiMessage("Get monthly revenue reports by restaurant id")
    public ResponseEntity<List<MonthlyRevenueReport>> getMonthlyRevenueReportsByRestaurantId(
            @PathVariable("restaurantId") Long restaurantId) {
        List<MonthlyRevenueReport> reports = monthlyRevenueReportService
                .getMonthlyRevenueReportsByRestaurantId(restaurantId);
        return ResponseEntity.ok(reports);
    }

    @GetMapping("/monthly-revenue-reports/restaurant/{restaurantId}/month-year")
    @ApiMessage("Get monthly revenue report by restaurant, month and year")
    public ResponseEntity<MonthlyRevenueReport> getMonthlyRevenueReportByRestaurantAndMonthYear(
            @PathVariable("restaurantId") Long restaurantId,
            @RequestParam Integer month,
            @RequestParam Integer year) throws IdInvalidException {
        MonthlyRevenueReport report = monthlyRevenueReportService
                .getMonthlyRevenueReportByRestaurantAndMonthYear(restaurantId, month, year);
        if (report == null) {
            throw new IdInvalidException("Monthly revenue report not found for restaurant id: " + restaurantId
                    + ", month: " + month + ", year: " + year);
        }
        return ResponseEntity.ok(report);
    }

    @GetMapping("/monthly-revenue-reports/year/{year}")
    @ApiMessage("Get monthly revenue reports by year")
    public ResponseEntity<List<MonthlyRevenueReport>> getMonthlyRevenueReportsByYear(
            @PathVariable("year") Integer year) {
        List<MonthlyRevenueReport> reports = monthlyRevenueReportService.getMonthlyRevenueReportsByYear(year);
        return ResponseEntity.ok(reports);
    }

    @GetMapping("/monthly-revenue-reports/month-year")
    @ApiMessage("Get monthly revenue reports by month and year")
    public ResponseEntity<List<MonthlyRevenueReport>> getMonthlyRevenueReportsByMonthAndYear(
            @RequestParam Integer month,
            @RequestParam Integer year) {
        List<MonthlyRevenueReport> reports = monthlyRevenueReportService.getMonthlyRevenueReportsByMonthAndYear(month,
                year);
        return ResponseEntity.ok(reports);
    }

    @GetMapping("/monthly-revenue-reports/restaurant/{restaurantId}/year/{year}/total")
    @ApiMessage("Get total revenue by restaurant and year")
    public ResponseEntity<BigDecimal> getTotalRevenueByRestaurantAndYear(
            @PathVariable("restaurantId") Long restaurantId,
            @PathVariable("year") Integer year) {
        BigDecimal total = monthlyRevenueReportService.getTotalRevenueByRestaurantAndYear(restaurantId, year);
        return ResponseEntity.ok(total);
    }

    @DeleteMapping("/monthly-revenue-reports/{id}")
    @ApiMessage("Delete monthly revenue report by id")
    public ResponseEntity<Void> deleteMonthlyRevenueReport(@PathVariable("id") Long id) throws IdInvalidException {
        MonthlyRevenueReport report = monthlyRevenueReportService.getMonthlyRevenueReportById(id);
        if (report == null) {
            throw new IdInvalidException("Monthly revenue report not found with id: " + id);
        }
        monthlyRevenueReportService.deleteMonthlyRevenueReport(id);
        return ResponseEntity.ok().body(null);
    }
}
