package com.example.FoodDelivery.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.FoodDelivery.domain.MonthlyRevenueReport;
import com.example.FoodDelivery.domain.OrderEarningsSummary;
import com.example.FoodDelivery.domain.Restaurant;
import com.example.FoodDelivery.domain.res.ResultPaginationDTO;
import com.example.FoodDelivery.repository.MonthlyRevenueReportRepository;
import com.example.FoodDelivery.util.error.IdInvalidException;

@Service
public class MonthlyRevenueReportService {
    private final MonthlyRevenueReportRepository monthlyRevenueReportRepository;
    private final RestaurantService restaurantService;
    private final OrderEarningsSummaryService orderEarningsSummaryService;

    public MonthlyRevenueReportService(MonthlyRevenueReportRepository monthlyRevenueReportRepository,
            RestaurantService restaurantService,
            OrderEarningsSummaryService orderEarningsSummaryService) {
        this.monthlyRevenueReportRepository = monthlyRevenueReportRepository;
        this.restaurantService = restaurantService;
        this.orderEarningsSummaryService = orderEarningsSummaryService;
    }

    public boolean existsByRestaurantIdAndMonthAndYear(Long restaurantId, Integer month, Integer year) {
        return monthlyRevenueReportRepository.existsByRestaurantIdAndMonthAndYear(restaurantId, month, year);
    }

    public MonthlyRevenueReport getMonthlyRevenueReportById(Long id) {
        Optional<MonthlyRevenueReport> reportOpt = this.monthlyRevenueReportRepository.findById(id);
        return reportOpt.orElse(null);
    }

    public MonthlyRevenueReport getMonthlyRevenueReportByRestaurantAndMonthYear(Long restaurantId, Integer month,
            Integer year) {
        Optional<MonthlyRevenueReport> reportOpt = this.monthlyRevenueReportRepository
                .findByRestaurantIdAndMonthAndYear(restaurantId, month, year);
        return reportOpt.orElse(null);
    }

    public List<MonthlyRevenueReport> getMonthlyRevenueReportsByRestaurantId(Long restaurantId) {
        return this.monthlyRevenueReportRepository.findByRestaurantIdOrderByYearDescMonthDesc(restaurantId);
    }

    public List<MonthlyRevenueReport> getMonthlyRevenueReportsByYear(Integer year) {
        return this.monthlyRevenueReportRepository.findByYear(year);
    }

    public List<MonthlyRevenueReport> getMonthlyRevenueReportsByMonthAndYear(Integer month, Integer year) {
        return this.monthlyRevenueReportRepository.findByMonthAndYear(month, year);
    }

    public BigDecimal getTotalRevenueByRestaurantAndYear(Long restaurantId, Integer year) {
        BigDecimal total = this.monthlyRevenueReportRepository.sumTotalRevenueByRestaurantAndYear(restaurantId, year);
        return total != null ? total : BigDecimal.ZERO;
    }

    @Transactional
    public MonthlyRevenueReport createMonthlyRevenueReport(MonthlyRevenueReport monthlyRevenueReport)
            throws IdInvalidException {
        // check restaurant exists
        if (monthlyRevenueReport.getRestaurant() != null) {
            Restaurant restaurant = this.restaurantService
                    .getRestaurantById(monthlyRevenueReport.getRestaurant().getId());
            if (restaurant == null) {
                throw new IdInvalidException(
                        "Restaurant not found with id: " + monthlyRevenueReport.getRestaurant().getId());
            }
            monthlyRevenueReport.setRestaurant(restaurant);
        } else {
            throw new IdInvalidException("Restaurant is required");
        }

        // validate month and year
        if (monthlyRevenueReport.getMonth() == null || monthlyRevenueReport.getMonth() < 1
                || monthlyRevenueReport.getMonth() > 12) {
            throw new IdInvalidException("Month must be between 1 and 12");
        }
        if (monthlyRevenueReport.getYear() == null || monthlyRevenueReport.getYear() < 2000) {
            throw new IdInvalidException("Invalid year");
        }

        // check duplicate report
        if (this.existsByRestaurantIdAndMonthAndYear(
                monthlyRevenueReport.getRestaurant().getId(),
                monthlyRevenueReport.getMonth(),
                monthlyRevenueReport.getYear())) {
            throw new IdInvalidException("Monthly revenue report already exists for this restaurant, month and year");
        }

        return monthlyRevenueReportRepository.save(monthlyRevenueReport);
    }

    @Transactional
    public MonthlyRevenueReport generateMonthlyRevenueReport(Long restaurantId, Integer month, Integer year)
            throws IdInvalidException {
        // check restaurant exists
        Restaurant restaurant = this.restaurantService.getRestaurantById(restaurantId);
        if (restaurant == null) {
            throw new IdInvalidException("Restaurant not found with id: " + restaurantId);
        }

        // validate month and year
        if (month < 1 || month > 12) {
            throw new IdInvalidException("Month must be between 1 and 12");
        }
        if (year < 2000) {
            throw new IdInvalidException("Invalid year");
        }

        // check if report already exists
        if (this.existsByRestaurantIdAndMonthAndYear(restaurantId, month, year)) {
            throw new IdInvalidException("Monthly revenue report already exists for this restaurant, month and year");
        }

        // calculate date range for the month
        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();

        Instant startInstant = startDate.atStartOfDay(ZoneId.systemDefault()).toInstant();
        Instant endInstant = endDate.atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant();

        // get all order earnings summaries for this restaurant in this month
        List<OrderEarningsSummary> summaries = this.orderEarningsSummaryService
                .getOrderEarningsSummariesByDateRange(startInstant, endInstant)
                .stream()
                .filter(s -> s.getRestaurant().getId().equals(restaurantId))
                .toList();

        // calculate totals
        BigDecimal totalRevenue = summaries.stream()
                .map(OrderEarningsSummary::getOrderSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalCommission = summaries.stream()
                .map(OrderEarningsSummary::getRestaurantCommissionAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal netPayout = summaries.stream()
                .map(OrderEarningsSummary::getRestaurantNetEarning)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Integer totalOrders = summaries.size();

        // create report
        MonthlyRevenueReport report = MonthlyRevenueReport.builder()
                .restaurant(restaurant)
                .month(month)
                .year(year)
                .totalRevenue(totalRevenue)
                .totalCommission(totalCommission)
                .netPayout(netPayout)
                .totalOrders(totalOrders)
                .build();

        return monthlyRevenueReportRepository.save(report);
    }

    @Transactional
    public MonthlyRevenueReport updateMonthlyRevenueReport(MonthlyRevenueReport monthlyRevenueReport)
            throws IdInvalidException {
        // check id
        MonthlyRevenueReport currentReport = getMonthlyRevenueReportById(monthlyRevenueReport.getId());
        if (currentReport == null) {
            throw new IdInvalidException("Monthly revenue report not found with id: " + monthlyRevenueReport.getId());
        }

        // update fields
        if (monthlyRevenueReport.getTotalRevenue() != null) {
            currentReport.setTotalRevenue(monthlyRevenueReport.getTotalRevenue());
        }
        if (monthlyRevenueReport.getTotalCommission() != null) {
            currentReport.setTotalCommission(monthlyRevenueReport.getTotalCommission());
        }
        if (monthlyRevenueReport.getNetPayout() != null) {
            currentReport.setNetPayout(monthlyRevenueReport.getNetPayout());
        }
        if (monthlyRevenueReport.getTotalOrders() != null) {
            currentReport.setTotalOrders(monthlyRevenueReport.getTotalOrders());
        }

        return monthlyRevenueReportRepository.save(currentReport);
    }

    @Transactional
    public MonthlyRevenueReport regenerateMonthlyRevenueReport(Long reportId) throws IdInvalidException {
        MonthlyRevenueReport report = getMonthlyRevenueReportById(reportId);
        if (report == null) {
            throw new IdInvalidException("Monthly revenue report not found with id: " + reportId);
        }

        Long restaurantId = report.getRestaurant().getId();
        Integer month = report.getMonth();
        Integer year = report.getYear();

        // delete old report
        this.deleteMonthlyRevenueReport(reportId);

        // generate new report
        return this.generateMonthlyRevenueReport(restaurantId, month, year);
    }

    public ResultPaginationDTO getAllMonthlyRevenueReports(Specification<MonthlyRevenueReport> spec,
            Pageable pageable) {
        Page<MonthlyRevenueReport> page = this.monthlyRevenueReportRepository.findAll(spec, pageable);
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

    public void deleteMonthlyRevenueReport(Long id) {
        this.monthlyRevenueReportRepository.deleteById(id);
    }
}
