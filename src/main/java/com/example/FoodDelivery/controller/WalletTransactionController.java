package com.example.FoodDelivery.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.turkraft.springfilter.boot.Filter;

import com.example.FoodDelivery.domain.WalletTransaction;
import com.example.FoodDelivery.domain.res.ResultPaginationDTO;
import com.example.FoodDelivery.service.WalletTransactionService;
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

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1")
public class WalletTransactionController {
    private final WalletTransactionService walletTransactionService;

    public WalletTransactionController(WalletTransactionService walletTransactionService) {
        this.walletTransactionService = walletTransactionService;
    }

    @PostMapping("/wallet-transactions")
    @ApiMessage("Create wallet transaction")
    public ResponseEntity<WalletTransaction> createWalletTransaction(@RequestBody WalletTransaction walletTransaction)
            throws IdInvalidException {
        WalletTransaction createdTransaction = walletTransactionService.createWalletTransaction(walletTransaction);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdTransaction);
    }

    @PutMapping("/wallet-transactions")
    @ApiMessage("Update wallet transaction")
    public ResponseEntity<WalletTransaction> updateWalletTransaction(@RequestBody WalletTransaction walletTransaction)
            throws IdInvalidException {
        WalletTransaction updatedTransaction = walletTransactionService.updateWalletTransaction(walletTransaction);
        return ResponseEntity.ok(updatedTransaction);
    }

    @PostMapping("/wallet-transactions/deposit")
    @ApiMessage("Deposit to wallet")
    public ResponseEntity<WalletTransaction> depositToWallet(@RequestBody Map<String, Object> body)
            throws IdInvalidException {
        Long walletId = Long.valueOf(body.get("walletId").toString());
        BigDecimal amount = new BigDecimal(body.get("amount").toString());
        String description = body.get("description") != null ? body.get("description").toString() : null;

        WalletTransaction transaction = walletTransactionService.depositToWallet(walletId, amount, description);
        return ResponseEntity.status(HttpStatus.CREATED).body(transaction);
    }

    @PostMapping("/wallet-transactions/withdraw")
    @ApiMessage("Withdraw from wallet")
    public ResponseEntity<WalletTransaction> withdrawFromWallet(@RequestBody Map<String, Object> body)
            throws IdInvalidException {
        Long walletId = Long.valueOf(body.get("walletId").toString());
        BigDecimal amount = new BigDecimal(body.get("amount").toString());
        String description = body.get("description") != null ? body.get("description").toString() : null;

        WalletTransaction transaction = walletTransactionService.withdrawFromWallet(walletId, amount, description);
        return ResponseEntity.status(HttpStatus.CREATED).body(transaction);
    }

    @PostMapping("/wallet-transactions/payment")
    @ApiMessage("Payment for order")
    public ResponseEntity<WalletTransaction> paymentForOrder(@RequestBody Map<String, Object> body)
            throws IdInvalidException {
        Long walletId = Long.valueOf(body.get("walletId").toString());
        Long orderId = Long.valueOf(body.get("orderId").toString());
        BigDecimal amount = new BigDecimal(body.get("amount").toString());

        WalletTransaction transaction = walletTransactionService.paymentForOrder(walletId, orderId, amount);
        return ResponseEntity.status(HttpStatus.CREATED).body(transaction);
    }

    @PostMapping("/wallet-transactions/refund")
    @ApiMessage("Refund for order")
    public ResponseEntity<WalletTransaction> refundForOrder(@RequestBody Map<String, Object> body)
            throws IdInvalidException {
        Long walletId = Long.valueOf(body.get("walletId").toString());
        Long orderId = Long.valueOf(body.get("orderId").toString());
        BigDecimal amount = new BigDecimal(body.get("amount").toString());

        WalletTransaction transaction = walletTransactionService.refundForOrder(walletId, orderId, amount);
        return ResponseEntity.status(HttpStatus.CREATED).body(transaction);
    }

    @GetMapping("/wallet-transactions")
    @ApiMessage("Get all wallet transactions")
    public ResponseEntity<ResultPaginationDTO> getAllWalletTransactions(
            @Filter Specification<WalletTransaction> spec, Pageable pageable) {
        ResultPaginationDTO result = walletTransactionService.getAllWalletTransactions(spec, pageable);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/wallet-transactions/{id}")
    @ApiMessage("Get wallet transaction by id")
    public ResponseEntity<WalletTransaction> getWalletTransactionById(@PathVariable("id") Long id)
            throws IdInvalidException {
        WalletTransaction transaction = walletTransactionService.getWalletTransactionById(id);
        if (transaction == null) {
            throw new IdInvalidException("Wallet transaction not found with id: " + id);
        }
        return ResponseEntity.ok(transaction);
    }

    @GetMapping("/wallet-transactions/wallet/{walletId}")
    @ApiMessage("Get wallet transactions by wallet id")
    public ResponseEntity<List<WalletTransaction>> getWalletTransactionsByWalletId(
            @PathVariable("walletId") Long walletId) {
        List<WalletTransaction> transactions = walletTransactionService.getWalletTransactionsByWalletId(walletId);
        return ResponseEntity.ok(transactions);
    }

    @GetMapping("/wallet-transactions/order/{orderId}")
    @ApiMessage("Get wallet transactions by order id")
    public ResponseEntity<List<WalletTransaction>> getWalletTransactionsByOrderId(
            @PathVariable("orderId") Long orderId) {
        List<WalletTransaction> transactions = walletTransactionService.getWalletTransactionsByOrderId(orderId);
        return ResponseEntity.ok(transactions);
    }

    @DeleteMapping("/wallet-transactions/{id}")
    @ApiMessage("Delete wallet transaction by id")
    public ResponseEntity<Void> deleteWalletTransaction(@PathVariable("id") Long id) throws IdInvalidException {
        WalletTransaction transaction = walletTransactionService.getWalletTransactionById(id);
        if (transaction == null) {
            throw new IdInvalidException("Wallet transaction not found with id: " + id);
        }
        walletTransactionService.deleteWalletTransaction(id);
        return ResponseEntity.ok().body(null);
    }
}
