package com.hsbc.transaction.service;

import com.hsbc.transaction.dto.TransactionRequest;
import com.hsbc.transaction.entity.IncomingTransaction;
import com.hsbc.transaction.entity.OutgoingTransaction;
import com.hsbc.transaction.repository.IncomingTransactionRepository;
import com.hsbc.transaction.repository.OutgoingTransactionRepository;
import com.hsbc.transaction.repository.AccountRepository;
import com.hsbc.transaction.repository.TransactionLogRepository;
import com.hsbc.transaction.service.impl.TransactionServiceImpl;
import com.hsbc.transaction.enums.TransactionType;
import com.hsbc.transaction.entity.Account;
import com.hsbc.transaction.exception.InvalidTransactionException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import jakarta.persistence.EntityNotFoundException;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private OutgoingTransactionRepository outgoingTransactionRepository;

    @Mock
    private IncomingTransactionRepository incomingTransactionRepository;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private TransactionLogRepository transactionLogRepository;

    @InjectMocks
    private TransactionServiceImpl transactionService;

    private TransactionRequest request;
    private OutgoingTransaction outgoingTransaction;
    private IncomingTransaction incomingTransaction;

    @BeforeEach
    void setUp() {
        request = new TransactionRequest();
        request.setAccountId(1L);
        request.setCustomerId(1L);
        request.setAmount(new BigDecimal("100.00"));
        request.setTransactionType(TransactionType.TRANSFER);
        request.setTargetAccountId(2L);

        outgoingTransaction = OutgoingTransaction.builder()
                .transactionId(1L)
                .accountId(1L)
                .customerId(1L)
                .amount(new BigDecimal("100.00"))
                .build();

        incomingTransaction = IncomingTransaction.builder()
                .transactionId(1L)
                .accountId(2L)
                .customerId(1L)
                .amount(new BigDecimal("100.00"))
                .build();
    }

    @Test
    void createTransaction_Success() {
        Account sourceAccount = new Account();
        sourceAccount.setBalance(new BigDecimal("1000.00"));
        sourceAccount.setCurrency("CNY");
        
        Account targetAccount = new Account();
        targetAccount.setBalance(new BigDecimal("1000.00"));
        targetAccount.setCurrency("CNY");

        when(outgoingTransactionRepository.save(any(OutgoingTransaction.class)))
                .thenReturn(outgoingTransaction);
        when(incomingTransactionRepository.save(any(IncomingTransaction.class)))
                .thenReturn(incomingTransaction);
        when(accountRepository.findById(1L))
                .thenReturn(Optional.of(sourceAccount));
        when(accountRepository.findById(2L))
                .thenReturn(Optional.of(targetAccount));

        assertDoesNotThrow(() -> transactionService.createTransaction(request));

        verify(outgoingTransactionRepository, times(1)).save(any(OutgoingTransaction.class));
        verify(incomingTransactionRepository).save(any(IncomingTransaction.class));
        verify(accountRepository, atLeastOnce()).findById(anyLong());
    }

    @Test
    void createTransaction_DifferentCurrency_ShouldThrowException() {
        Account sourceAccount = new Account();
        sourceAccount.setBalance(new BigDecimal("1000.00"));
        sourceAccount.setCurrency("CNY");
        
        Account targetAccount = new Account();
        targetAccount.setBalance(new BigDecimal("1000.00"));
        targetAccount.setCurrency("USD");

        when(accountRepository.findById(1L))
                .thenReturn(Optional.of(sourceAccount));
        when(accountRepository.findById(2L))
                .thenReturn(Optional.of(targetAccount));

        assertThrows(InvalidTransactionException.class, () -> 
                transactionService.createTransaction(request));
    }

    @Test
    void deleteTransaction_OutgoingNotFound() {
        when(outgoingTransactionRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () ->
                transactionService.deleteTransaction(1L));
    }

    @Test
    void listOutgoingTransactions_Success() {
        List<OutgoingTransaction> transactions = Arrays.asList(outgoingTransaction);
        when(outgoingTransactionRepository.findByAccountIdAndDelFlagFalse(1L))
                .thenReturn(transactions);

        List<OutgoingTransaction> result = transactionService.listOutgoingTransactions(1L);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(outgoingTransactionRepository).findByAccountIdAndDelFlagFalse(1L);
    }

    @Test
    void listIncomingTransactions_Success() {
        List<IncomingTransaction> transactions = Arrays.asList(incomingTransaction);
        when(incomingTransactionRepository.findByAccountIdAndDelFlagFalse(2L))
                .thenReturn(transactions);

        List<IncomingTransaction> result = transactionService.listIncomingTransactions(2L);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(incomingTransactionRepository).findByAccountIdAndDelFlagFalse(2L);
    }
} 