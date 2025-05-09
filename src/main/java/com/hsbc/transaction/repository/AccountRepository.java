package com.hsbc.transaction.repository;

import com.hsbc.transaction.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {
    List<Account> findByCustomerIdAndDelFlagFalse(Long customerId);
    List<Account> findByDelFlagFalse();
    Optional<Account> findByAccountIdAndDelFlagFalse(Long accountId);
} 