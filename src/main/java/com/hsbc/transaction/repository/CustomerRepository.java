package com.hsbc.transaction.repository;

import com.hsbc.transaction.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {
    Optional<Customer> findByEmailAndDelFlag(String email, Integer delFlag);
    Optional<Customer> findByPhoneNumberAndDelFlag(String phoneNumber, Integer delFlag);
    Optional<Customer> findByCustomerIdAndDelFlag(Long customerId, Integer delFlag);
} 