package com.example.FoodDelivery.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.turkraft.springfilter.boot.Filter;

import jakarta.validation.Valid;
import com.example.FoodDelivery.domain.Review;
import com.example.FoodDelivery.domain.res.ResultPaginationDTO;
import com.example.FoodDelivery.service.ReviewService;
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
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class ReviewController {
    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @PostMapping("/reviews")
    @ApiMessage("Create new review")
    public ResponseEntity<Review> createReview(@Valid @RequestBody Review review)
            throws IdInvalidException {
        Review createdReview = reviewService.createReview(review);
        return ResponseEntity.ok(createdReview);
    }

    @PutMapping("/reviews")
    @ApiMessage("Update review")
    public ResponseEntity<Review> updateReview(@RequestBody Review review)
            throws IdInvalidException {
        Review updatedReview = reviewService.updateReview(review);
        return ResponseEntity.ok(updatedReview);
    }

    @GetMapping("/reviews")
    @ApiMessage("Get all reviews")
    public ResponseEntity<ResultPaginationDTO> getAllReviews(
            @Filter Specification<Review> spec, Pageable pageable) {
        ResultPaginationDTO result = reviewService.getAllReviews(spec, pageable);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/reviews/{id}")
    @ApiMessage("Get review by id")
    public ResponseEntity<Review> getReviewById(@PathVariable("id") Long id) throws IdInvalidException {
        Review review = reviewService.getReviewById(id);
        if (review == null) {
            throw new IdInvalidException("Review not found with id: " + id);
        }
        return ResponseEntity.ok(review);
    }

    @GetMapping("/reviews/customer/{customerId}")
    @ApiMessage("Get reviews by customer id")
    public ResponseEntity<List<Review>> getReviewsByCustomerId(@PathVariable("customerId") Long customerId) {
        List<Review> reviews = reviewService.getReviewsByCustomerId(customerId);
        return ResponseEntity.ok(reviews);
    }

    @GetMapping("/reviews/order/{orderId}")
    @ApiMessage("Get reviews by order id")
    public ResponseEntity<List<Review>> getReviewsByOrderId(@PathVariable("orderId") Long orderId) {
        List<Review> reviews = reviewService.getReviewsByOrderId(orderId);
        return ResponseEntity.ok(reviews);
    }

    @GetMapping("/reviews/target")
    @ApiMessage("Get reviews by target")
    public ResponseEntity<List<Review>> getReviewsByTarget(
            @RequestParam("reviewTarget") String reviewTarget,
            @RequestParam("targetId") Long targetId) {
        List<Review> reviews = reviewService.getReviewsByTarget(reviewTarget, targetId);
        return ResponseEntity.ok(reviews);
    }

    @DeleteMapping("/reviews/{id}")
    @ApiMessage("Delete review by id")
    public ResponseEntity<Void> deleteReview(@PathVariable("id") Long id) throws IdInvalidException {
        Review review = reviewService.getReviewById(id);
        if (review == null) {
            throw new IdInvalidException("Review not found with id: " + id);
        }
        reviewService.deleteReview(id);
        return ResponseEntity.ok().body(null);
    }
}
