package com.hsbc.transaction.controller;

import com.hsbc.transaction.dto.TransactionRequest;
import com.hsbc.transaction.entity.IncomingTransaction;
import com.hsbc.transaction.entity.OutgoingTransaction;
import com.hsbc.transaction.entity.Transaction;
import com.hsbc.transaction.service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/transactions")
@RequiredArgsConstructor
@Tag(name = "Transaction Management", description = "API For Transaction Management")
public class TransactionController {

    private final TransactionService transactionService;

    @PostMapping
    @Operation(summary = "Create Transaction")
    public ResponseEntity<Void> createTransaction(@Validated @RequestBody TransactionRequest request) {
        transactionService.createTransaction(request);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{transactionId}")
    @Operation(summary = "Delete Transaction")
    public ResponseEntity<Void> deleteTransaction(@PathVariable Long transactionId) {
        transactionService.deleteTransaction(transactionId);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{transactionId}")
    @Operation(summary = "Modify Transaction")
    public ResponseEntity<Void> modifyTransaction(
            @PathVariable Long transactionId,
            @Validated @RequestBody TransactionRequest request) {
        transactionService.modifyTransaction(transactionId, request);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/outgoing")
    @Operation(summary = "List Outgoing Transactions")
    public ResponseEntity<List<OutgoingTransaction>> listOutgoingTransactions(
            @RequestParam Long accountId) {
        return ResponseEntity.ok(transactionService.listOutgoingTransactions(accountId));
    }

    @GetMapping("/incoming")
    @Operation(summary = "List Incoming Transactions")
    public ResponseEntity<List<IncomingTransaction>> listIncomingTransactions(
            @RequestParam Long accountId) {
        return ResponseEntity.ok(transactionService.listIncomingTransactions(accountId));
    }

    @GetMapping("/all")
    @Operation(summary = "List All Transactions")
    public ResponseEntity<List<Transaction>> listAllTransactions(
            @RequestParam Long accountId) {
        return ResponseEntity.ok(transactionService.listAllTransactions(accountId));
    }
}
