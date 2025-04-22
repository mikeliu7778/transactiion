package com.hsbc.transaction.service.impl;

import com.hsbc.transaction.dto.CustomerDTO;
import com.hsbc.transaction.entity.Customer;
import com.hsbc.transaction.repository.CustomerRepository;
import com.hsbc.transaction.service.CustomerService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;

    public CustomerServiceImpl(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    @Override
    @Transactional
    public CustomerDTO createCustomer(CustomerDTO customerDTO) {
        // 检查邮箱和电话是否已存在
        customerRepository.findByEmailAndDelFlag(customerDTO.getEmail(), 0)
                .ifPresent(c -> {
                    throw new IllegalArgumentException("邮箱已存在");
                });
        customerRepository.findByPhoneNumberAndDelFlag(customerDTO.getPhoneNumber(), 0)
                .ifPresent(c -> {
                    throw new IllegalArgumentException("电话号码已存在");
                });

        Customer customer = new Customer();
        BeanUtils.copyProperties(customerDTO, customer);
        customer = customerRepository.save(customer);
        CustomerDTO result = new CustomerDTO();
        BeanUtils.copyProperties(customer, result);
        return result;
    }

    @Override
    @Transactional
    public CustomerDTO updateCustomer(Long customerId, CustomerDTO customerDTO) {
        Customer customer = customerRepository.findByCustomerIdAndDelFlag(customerId, 0)
                .orElseThrow(() -> new EntityNotFoundException("客户不存在"));

        // 检查邮箱和电话是否与其他用户重复
        customerRepository.findByEmailAndDelFlag(customerDTO.getEmail(), 0)
                .ifPresent(c -> {
                    if (!c.getCustomerId().equals(customerId)) {
                        throw new IllegalArgumentException("邮箱已存在");
                    }
                });
        customerRepository.findByPhoneNumberAndDelFlag(customerDTO.getPhoneNumber(), 0)
                .ifPresent(c -> {
                    if (!c.getCustomerId().equals(customerId)) {
                        throw new IllegalArgumentException("电话号码已存在");
                    }
                });

        BeanUtils.copyProperties(customerDTO, customer, "customerId", "delFlag", "createdAt");
        customer = customerRepository.save(customer);
        CustomerDTO result = new CustomerDTO();
        BeanUtils.copyProperties(customer, result);
        return result;
    }

    @Override
    public CustomerDTO getCustomerById(Long customerId) {
        Customer customer = customerRepository.findByCustomerIdAndDelFlag(customerId, 0)
                .orElseThrow(() -> new EntityNotFoundException("客户不存在"));
        CustomerDTO customerDTO = new CustomerDTO();
        BeanUtils.copyProperties(customer, customerDTO);
        return customerDTO;
    }

    @Override
    @Transactional
    public void deleteCustomer(Long customerId) {
        Customer customer = customerRepository.findByCustomerIdAndDelFlag(customerId, 0)
                .orElseThrow(() -> new EntityNotFoundException("客户不存在"));
        customer.setDelFlag(1);
        customerRepository.save(customer);
    }
} 