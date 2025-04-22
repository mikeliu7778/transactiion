package com.hsbc.transaction.service;

import com.hsbc.transaction.dto.CustomerDTO;
import java.util.List;

public interface CustomerService {
    CustomerDTO createCustomer(CustomerDTO customerDTO);
    CustomerDTO updateCustomer(Long customerId, CustomerDTO customerDTO);
    CustomerDTO getCustomerById(Long customerId);
    void deleteCustomer(Long customerId);
} 