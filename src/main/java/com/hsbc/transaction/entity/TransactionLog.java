package com.hsbc.transaction.entity;

import com.hsbc.transaction.enums.TransactionStatus;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "TransactionLogs")
public class TransactionLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long logId;

    private Long transactionId;

    @Enumerated(EnumType.STRING)
    private TransactionStatus status;

    @Column(columnDefinition = "TEXT")
    private String message;

    private Boolean delFlag = false;

    private LocalDateTime loggedAt;

    @PrePersist
    protected void onCreate() {
        loggedAt = LocalDateTime.now();
    }
} 