package com.example.FoodDelivery.service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.example.FoodDelivery.domain.Review;
import com.example.FoodDelivery.domain.User;
import com.example.FoodDelivery.domain.Order;
import com.example.FoodDelivery.domain.res.ResultPaginationDTO;
import com.example.FoodDelivery.repository.ReviewRepository;
import com.example.FoodDelivery.repository.UserRepository;
import com.example.FoodDelivery.repository.OrderRepository;
import com.example.FoodDelivery.util.error.IdInvalidException;

@Service
public class ReviewService {
    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;

    public ReviewService(ReviewRepository reviewRepository, UserRepository userRepository,
            OrderRepository orderRepository) {
        this.reviewRepository = reviewRepository;
        this.userRepository = userRepository;
        this.orderRepository = orderRepository;
    }

    public Review getReviewById(Long id) {
        Optional<Review> reviewOpt = this.reviewRepository.findById(id);
        return reviewOpt.orElse(null);
    }

    public List<Review> getReviewsByCustomerId(Long customerId) {
        return this.reviewRepository.findByCustomerId(customerId);
    }

    public List<Review> getReviewsByOrderId(Long orderId) {
        return this.reviewRepository.findByOrderId(orderId);
    }

    public List<Review> getReviewsByTarget(String reviewTarget, Long targetId) {
        return this.reviewRepository.findByReviewTargetAndTargetId(reviewTarget, targetId);
    }

    public Review createReview(Review review) throws IdInvalidException {
        // check customer exists
        if (review.getCustomer() != null) {
            User customer = this.userRepository.findById(review.getCustomer().getId()).orElse(null);
            if (customer == null) {
                throw new IdInvalidException("Customer not found with id: " + review.getCustomer().getId());
            }
            review.setCustomer(customer);
        } else {
            throw new IdInvalidException("Customer is required");
        }

        // check order exists
        if (review.getOrder() != null) {
            Order order = this.orderRepository.findById(review.getOrder().getId()).orElse(null);
            if (order == null) {
                throw new IdInvalidException("Order not found with id: " + review.getOrder().getId());
            }
            review.setOrder(order);
        } else {
            throw new IdInvalidException("Order is required");
        }

        review.setCreatedAt(Instant.now());
        return reviewRepository.save(review);
    }

    public Review updateReview(Review review) throws IdInvalidException {
        // check id
        Review currentReview = getReviewById(review.getId());
        if (currentReview == null) {
            throw new IdInvalidException("Review not found with id: " + review.getId());
        }

        if (review.getReviewTarget() != null) {
            currentReview.setReviewTarget(review.getReviewTarget());
        }
        if (review.getTargetId() != null) {
            currentReview.setTargetId(review.getTargetId());
        }
        if (review.getRating() != null) {
            currentReview.setRating(review.getRating());
        }
        if (review.getComment() != null) {
            currentReview.setComment(review.getComment());
        }
        if (review.getReply() != null) {
            currentReview.setReply(review.getReply());
        }
        if (review.getCustomer() != null) {
            User customer = this.userRepository.findById(review.getCustomer().getId()).orElse(null);
            if (customer == null) {
                throw new IdInvalidException("Customer not found with id: " + review.getCustomer().getId());
            }
            currentReview.setCustomer(customer);
        }
        if (review.getOrder() != null) {
            Order order = this.orderRepository.findById(review.getOrder().getId()).orElse(null);
            if (order == null) {
                throw new IdInvalidException("Order not found with id: " + review.getOrder().getId());
            }
            currentReview.setOrder(order);
        }

        return reviewRepository.save(currentReview);
    }

    public ResultPaginationDTO getAllReviews(Specification<Review> spec, Pageable pageable) {
        Page<Review> page = this.reviewRepository.findAll(spec, pageable);
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

    public void deleteReview(Long id) {
        this.reviewRepository.deleteById(id);
    }
}
