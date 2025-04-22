package com.hsbc.transaction.service;

import com.hsbc.transaction.dto.AccountDTO;
import java.util.List;

public interface AccountService {
    AccountDTO createAccount(AccountDTO accountDTO);
    AccountDTO updateAccount(Long accountId, AccountDTO accountDTO);
    AccountDTO getAccountById(Long accountId);
    List<AccountDTO> getAccountsByCustomerId(Long customerId);
    void deleteAccount(Long accountId);
} 