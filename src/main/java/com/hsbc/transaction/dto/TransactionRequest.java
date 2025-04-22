package com.hsbc.transaction.dto;

import lombok.Data;
import com.hsbc.transaction.enums.TransactionType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

@Data
public class TransactionRequest {
    @NotNull(message = "Account ID is required")
    private Long accountId;

    @NotNull(message = "Customer ID is required")
    private Long customerId;

    @NotNull(message = "Transaction type is required")
    private TransactionType transactionType;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    private BigDecimal amount;

    private Long targetAccountId; // for transfers (toAccountId or fromAccountId)
} 