package com.example.FoodDelivery.service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.FoodDelivery.domain.Order;
import com.example.FoodDelivery.domain.Wallet;
import com.example.FoodDelivery.domain.WalletTransaction;
import com.example.FoodDelivery.domain.res.ResultPaginationDTO;
import com.example.FoodDelivery.repository.WalletTransactionRepository;
import com.example.FoodDelivery.util.error.IdInvalidException;

@Service
public class WalletTransactionService {
    private final WalletTransactionRepository walletTransactionRepository;
    private final WalletService walletService;
    private final OrderService orderService;

    public WalletTransactionService(WalletTransactionRepository walletTransactionRepository,
            WalletService walletService,
            @Lazy OrderService orderService) {
        this.walletTransactionRepository = walletTransactionRepository;
        this.walletService = walletService;
        this.orderService = orderService;
    }

    public WalletTransaction getWalletTransactionById(Long id) {
        Optional<WalletTransaction> transactionOpt = this.walletTransactionRepository.findById(id);
        return transactionOpt.orElse(null);
    }

    public List<WalletTransaction> getWalletTransactionsByWalletId(Long walletId) {
        return this.walletTransactionRepository.findByWalletIdOrderByCreatedAtDesc(walletId);
    }

    public List<WalletTransaction> getWalletTransactionsByOrderId(Long orderId) {
        return this.walletTransactionRepository.findByOrderId(orderId);
    }

    public List<WalletTransaction> getWalletTransactionsByWalletIdAndType(Long walletId, String transactionType) {
        return this.walletTransactionRepository.findByWalletIdAndTransactionType(walletId, transactionType);
    }

    @Transactional
    public WalletTransaction createWalletTransaction(WalletTransaction walletTransaction) throws IdInvalidException {
        // check wallet exists
        if (walletTransaction.getWallet() != null) {
            Wallet wallet = this.walletService.getWalletById(walletTransaction.getWallet().getId());
            if (wallet == null) {
                throw new IdInvalidException("Wallet not found with id: " + walletTransaction.getWallet().getId());
            }
            walletTransaction.setWallet(wallet);
        } else {
            throw new IdInvalidException("Wallet is required");
        }

        // check order exists (if provided)
        if (walletTransaction.getOrder() != null) {
            Order order = this.orderService.getOrderById(walletTransaction.getOrder().getId());
            if (order == null) {
                throw new IdInvalidException("Order not found with id: " + walletTransaction.getOrder().getId());
            }
            walletTransaction.setOrder(order);
        }

        // validate amount
        if (walletTransaction.getAmount() == null || walletTransaction.getAmount().compareTo(BigDecimal.ZERO) == 0) {
            throw new IdInvalidException("Amount is required and must not be zero");
        }

        // validate transaction type
        if (walletTransaction.getTransactionType() == null) {
            throw new IdInvalidException("Transaction type is required");
        }

        // set default values
        if (walletTransaction.getStatus() == null) {
            walletTransaction.setStatus("PENDING");
        }
        walletTransaction.setCreatedAt(Instant.now());

        // save transaction
        WalletTransaction savedTransaction = walletTransactionRepository.save(walletTransaction);

        // update wallet balance if transaction is successful
        if ("SUCCESS".equals(walletTransaction.getStatus())) {
            updateWalletBalance(walletTransaction);
        }

        return savedTransaction;
    }

    @Transactional
    public WalletTransaction updateWalletTransaction(WalletTransaction walletTransaction) throws IdInvalidException {
        // check id
        WalletTransaction currentTransaction = getWalletTransactionById(walletTransaction.getId());
        if (currentTransaction == null) {
            throw new IdInvalidException("Wallet transaction not found with id: " + walletTransaction.getId());
        }

        String oldStatus = currentTransaction.getStatus();

        // update fields
        if (walletTransaction.getStatus() != null) {
            currentTransaction.setStatus(walletTransaction.getStatus());
        }
        if (walletTransaction.getDescription() != null) {
            currentTransaction.setDescription(walletTransaction.getDescription());
        }

        WalletTransaction updatedTransaction = walletTransactionRepository.save(currentTransaction);

        // update wallet balance if status changed to SUCCESS
        if (!"SUCCESS".equals(oldStatus) && "SUCCESS".equals(walletTransaction.getStatus())) {
            updateWalletBalance(currentTransaction);
        }

        return updatedTransaction;
    }

    @Transactional
    public WalletTransaction depositToWallet(Long walletId, BigDecimal amount, String description)
            throws IdInvalidException {
        Wallet wallet = this.walletService.getWalletById(walletId);
        if (wallet == null) {
            throw new IdInvalidException("Wallet not found with id: " + walletId);
        }

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IdInvalidException("Amount must be greater than 0");
        }

        WalletTransaction transaction = WalletTransaction.builder()
                .wallet(wallet)
                .amount(amount)
                .transactionType("DEPOSIT")
                .description(description != null ? description : "Deposit to wallet")
                .status("SUCCESS")
                .createdAt(Instant.now())
                .build();

        // save transaction
        WalletTransaction savedTransaction = walletTransactionRepository.save(transaction);

        // update wallet balance
        walletService.addBalance(walletId, amount);

        return savedTransaction;
    }

    @Transactional
    public WalletTransaction withdrawFromWallet(Long walletId, BigDecimal amount, String description)
            throws IdInvalidException {
        Wallet wallet = this.walletService.getWalletById(walletId);
        if (wallet == null) {
            throw new IdInvalidException("Wallet not found with id: " + walletId);
        }

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IdInvalidException("Amount must be greater than 0");
        }

        if (wallet.getBalance().compareTo(amount) < 0) {
            throw new IdInvalidException("Insufficient balance");
        }

        WalletTransaction transaction = WalletTransaction.builder()
                .wallet(wallet)
                .amount(amount.negate()) // negative amount for withdrawal
                .transactionType("WITHDRAWAL")
                .description(description != null ? description : "Withdrawal from wallet")
                .status("SUCCESS")
                .createdAt(Instant.now())
                .build();

        // save transaction
        WalletTransaction savedTransaction = walletTransactionRepository.save(transaction);

        // update wallet balance
        walletService.subtractBalance(walletId, amount);

        return savedTransaction;
    }

    @Transactional
    public WalletTransaction paymentForOrder(Long walletId, Long orderId, BigDecimal amount) throws IdInvalidException {
        Wallet wallet = this.walletService.getWalletById(walletId);
        if (wallet == null) {
            throw new IdInvalidException("Wallet not found with id: " + walletId);
        }

        Order order = this.orderService.getOrderById(orderId);
        if (order == null) {
            throw new IdInvalidException("Order not found with id: " + orderId);
        }

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IdInvalidException("Amount must be greater than 0");
        }

        if (wallet.getBalance().compareTo(amount) < 0) {
            throw new IdInvalidException("Insufficient balance");
        }

        WalletTransaction transaction = WalletTransaction.builder()
                .wallet(wallet)
                .order(order)
                .amount(amount.negate()) // negative amount for payment
                .transactionType("PAYMENT")
                .description("Payment for order #" + orderId)
                .status("SUCCESS")
                .createdAt(Instant.now())
                .build();

        // save transaction
        WalletTransaction savedTransaction = walletTransactionRepository.save(transaction);

        // update wallet balance
        walletService.subtractBalance(walletId, amount);

        return savedTransaction;
    }

    @Transactional
    public WalletTransaction refundForOrder(Long walletId, Long orderId, BigDecimal amount) throws IdInvalidException {
        Wallet wallet = this.walletService.getWalletById(walletId);
        if (wallet == null) {
            throw new IdInvalidException("Wallet not found with id: " + walletId);
        }

        Order order = this.orderService.getOrderById(orderId);
        if (order == null) {
            throw new IdInvalidException("Order not found with id: " + orderId);
        }

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IdInvalidException("Amount must be greater than 0");
        }

        WalletTransaction transaction = WalletTransaction.builder()
                .wallet(wallet)
                .order(order)
                .amount(amount)
                .transactionType("REFUND")
                .description("Refund for order #" + orderId)
                .status("SUCCESS")
                .createdAt(Instant.now())
                .build();

        // save transaction
        WalletTransaction savedTransaction = walletTransactionRepository.save(transaction);

        // update wallet balance
        walletService.addBalance(walletId, amount);

        return savedTransaction;
    }

    private void updateWalletBalance(WalletTransaction transaction) throws IdInvalidException {
        Wallet wallet = transaction.getWallet();
        BigDecimal amount = transaction.getAmount();

        if (amount.compareTo(BigDecimal.ZERO) > 0) {
            // positive amount - add to balance
            walletService.addBalance(wallet.getId(), amount);
        } else if (amount.compareTo(BigDecimal.ZERO) < 0) {
            // negative amount - subtract from balance
            walletService.subtractBalance(wallet.getId(), amount.negate());
        }
    }

    public ResultPaginationDTO getAllWalletTransactions(Specification<WalletTransaction> spec, Pageable pageable) {
        Page<WalletTransaction> page = this.walletTransactionRepository.findAll(spec, pageable);
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

    public void deleteWalletTransaction(Long id) {
        this.walletTransactionRepository.deleteById(id);
    }
}
