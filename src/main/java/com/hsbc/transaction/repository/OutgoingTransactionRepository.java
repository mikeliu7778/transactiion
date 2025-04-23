package com.hsbc.transaction.repository;

import com.hsbc.transaction.entity.OutgoingTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OutgoingTransactionRepository extends JpaRepository<OutgoingTransaction, Long> {
    List<OutgoingTransaction> findByAccountIdAndDelFlagFalse(Long accountId);
    List<OutgoingTransaction> findByCustomerIdAndDelFlagFalse(Long customerId);
    List<OutgoingTransaction> findByAccountIdAndToAccountIdAndDelFlagFalse(Long accountId, Long toAccountId);
    Optional<OutgoingTransaction> findByTransactionIdAndDelFlagFalse(Long transactionId);
} 