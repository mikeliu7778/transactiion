package com.hsbc.transaction.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import com.hsbc.transaction.enums.TransactionType;

import jakarta.persistence.*;

@Data
@Entity
@Table(name = "OutgoingTransactions")
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
public class OutgoingTransaction extends Transaction {
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionType transactionType;

    @Column
    private Long toAccountId;
} 