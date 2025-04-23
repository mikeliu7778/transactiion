package com.hsbc.transaction.service;

import com.hsbc.transaction.dto.TransactionRequest;
import com.hsbc.transaction.entity.Account;
import com.hsbc.transaction.entity.IncomingTransaction;
import com.hsbc.transaction.entity.OutgoingTransaction;
import com.hsbc.transaction.entity.TransactionLog;
import com.hsbc.transaction.enums.TransactionStatus;
import com.hsbc.transaction.enums.TransactionType;
import com.hsbc.transaction.repository.AccountRepository;
import com.hsbc.transaction.repository.IncomingTransactionRepository;
import com.hsbc.transaction.repository.OutgoingTransactionRepository;
import com.hsbc.transaction.repository.TransactionLogRepository;
import com.hsbc.transaction.service.impl.TransactionServiceImpl;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
@TestPropertySource(locations = "classpath:application-test.yml")
class TransactionServiceIntegrationTest {

    @Autowired
    private OutgoingTransactionRepository outgoingTransactionRepository;

    @Autowired
    private IncomingTransactionRepository incomingTransactionRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private TransactionLogRepository transactionLogRepository;

    private TransactionServiceImpl transactionService;

    private Account sourceAccount;
    private Account targetAccount;
    private TransactionRequest request;

    @BeforeEach
    void setUp() {
        transactionService = new TransactionServiceImpl(
                outgoingTransactionRepository,
                incomingTransactionRepository,
                accountRepository,
                transactionLogRepository
        );

        // 创建源账户
        sourceAccount = new Account();
        sourceAccount.setBalance(new BigDecimal("1000.00"));
        sourceAccount.setCustomerId(1L);
        sourceAccount.setAccountType(Account.AccountType.SAVINGS);
        sourceAccount = accountRepository.save(sourceAccount);

        // 创建目标账户
        targetAccount = new Account();
        targetAccount.setBalance(new BigDecimal("1000.00"));
        targetAccount.setCustomerId(2L);
        targetAccount.setAccountType(Account.AccountType.SAVINGS);
        targetAccount = accountRepository.save(targetAccount);

        // 创建交易请求
        request = new TransactionRequest();
        request.setAccountId(sourceAccount.getAccountId());
        request.setCustomerId(sourceAccount.getCustomerId());
        request.setAmount(new BigDecimal("100.00"));
        request.setTransactionType(TransactionType.TRANSFER);
        request.setTargetAccountId(targetAccount.getAccountId());
    }

    @Test
    void createTransaction_Success() {
        // 执行交易
        transactionService.createTransaction(request);

        // 验证转出交易
        List<OutgoingTransaction> outgoingTransactions = outgoingTransactionRepository
                .findByAccountIdAndDelFlagFalse(sourceAccount.getAccountId());
        assertEquals(1, outgoingTransactions.size());
        OutgoingTransaction outgoing = outgoingTransactions.get(0);
        assertEquals(request.getAmount(), outgoing.getAmount());
        assertEquals(TransactionType.TRANSFER, outgoing.getTransactionType());

        // 验证转入交易
        List<IncomingTransaction> incomingTransactions = incomingTransactionRepository
                .findByAccountIdAndDelFlagFalse(targetAccount.getAccountId());
        assertEquals(1, incomingTransactions.size());
        IncomingTransaction incoming = incomingTransactions.get(0);
        assertEquals(request.getAmount(), incoming.getAmount());
        assertEquals(TransactionType.TRANSFER, incoming.getTransactionType());

        // 验证账户余额
        sourceAccount = accountRepository.findById(sourceAccount.getAccountId()).get();
        targetAccount = accountRepository.findById(targetAccount.getAccountId()).get();
        assertEquals(new BigDecimal("900.00"), sourceAccount.getBalance());
        assertEquals(new BigDecimal("1100.00"), targetAccount.getBalance());

        // 验证交易日志
        List<TransactionLog> logs = transactionLogRepository.findAll();
        assertEquals(1, logs.size());
        assertEquals(TransactionStatus.COMPLETED, logs.get(0).getStatus());
    }

    @Test
    void deleteTransaction_Success() {
        // 先创建交易
        transactionService.createTransaction(request);

        // 获取交易ID
        List<OutgoingTransaction> outgoingTransactions = outgoingTransactionRepository
                .findByAccountIdAndDelFlagFalse(sourceAccount.getAccountId());
        Long transactionId = outgoingTransactions.get(0).getTransactionId();

        // 删除交易
        transactionService.deleteTransaction(transactionId);

        // 验证交易被软删除
        Optional<OutgoingTransaction> deletedOutgoing = outgoingTransactionRepository.findByTransactionIdAndDelFlagFalse(transactionId);
        assertTrue(deletedOutgoing.isEmpty());

        // 验证转入交易也被软删除
        Optional<IncomingTransaction> incomingTransaction = incomingTransactionRepository
                .findByTransactionIdAndDelFlagFalse(transactionId);
        assertTrue(incomingTransaction.isEmpty());

        // 验证账户余额已恢复
        sourceAccount = accountRepository.findById(sourceAccount.getAccountId()).get();
        targetAccount = accountRepository.findById(targetAccount.getAccountId()).get();
        assertEquals(new BigDecimal("1000.00"), sourceAccount.getBalance());
        assertEquals(new BigDecimal("1000.00"), targetAccount.getBalance());

        // 验证交易日志
        List<TransactionLog> logs = transactionLogRepository.findAll();
        assertEquals(2, logs.size()); // 创建和删除各一条日志
        assertEquals(TransactionStatus.REVERSED, logs.get(1).getStatus());
    }

    @Test
    void deleteTransaction_NotFound() {
        assertThrows(EntityNotFoundException.class, () ->
                transactionService.deleteTransaction(999L));
    }

    @Test
    void deleteTransaction_AlreadyDeleted() {
        // 先创建交易
        transactionService.createTransaction(request);

        // 获取交易ID
        List<OutgoingTransaction> outgoingTransactions = outgoingTransactionRepository
                .findByAccountIdAndDelFlagFalse(sourceAccount.getAccountId());
        Long transactionId = outgoingTransactions.get(0).getTransactionId();

        // 第一次删除
        transactionService.deleteTransaction(transactionId);

        // 尝试再次删除
        assertThrows(EntityNotFoundException.class, () ->
                transactionService.deleteTransaction(transactionId));
    }

    @Test
    void modifyTransaction_Success() {
        // 先创建交易
        transactionService.createTransaction(request);

        // 获取交易ID
        List<OutgoingTransaction> outgoingTransactions = outgoingTransactionRepository
                .findByAccountIdAndDelFlagFalse(sourceAccount.getAccountId());
        Long transactionId = outgoingTransactions.get(0).getTransactionId();

        // 修改交易金额
        TransactionRequest modifyRequest = new TransactionRequest();
        modifyRequest.setAccountId(sourceAccount.getAccountId());
        modifyRequest.setCustomerId(sourceAccount.getCustomerId());
        modifyRequest.setAmount(new BigDecimal("200.00"));
        modifyRequest.setTransactionType(TransactionType.TRANSFER);
        modifyRequest.setTargetAccountId(targetAccount.getAccountId());

        // 执行修改
        transactionService.modifyTransaction(transactionId, modifyRequest);

        // 验证修改后的交易
        outgoingTransactions = outgoingTransactionRepository
                .findByAccountIdAndDelFlagFalse(sourceAccount.getAccountId());
        assertEquals(1, outgoingTransactions.size());
        assertEquals(new BigDecimal("200.00"), outgoingTransactions.get(0).getAmount());

        // 验证转入交易也被修改
        List<IncomingTransaction> incomingTransactions = incomingTransactionRepository
                .findByAccountIdAndDelFlagFalse(targetAccount.getAccountId());
        assertEquals(1, incomingTransactions.size());
        assertEquals(new BigDecimal("200.00"), incomingTransactions.get(0).getAmount());

        // 验证账户余额
        sourceAccount = accountRepository.findById(sourceAccount.getAccountId()).get();
        targetAccount = accountRepository.findById(targetAccount.getAccountId()).get();
        assertEquals(new BigDecimal("800.00"), sourceAccount.getBalance());
        assertEquals(new BigDecimal("1200.00"), targetAccount.getBalance());

        // 验证交易日志
        List<TransactionLog> logs = transactionLogRepository.findAll();
        assertEquals(3, logs.size()); // 创建、撤销原交易、创建新交易各一条日志
    }

    @Test
    void modifyTransaction_AlreadyDeleted() {
        // 先创建交易
        transactionService.createTransaction(request);

        // 获取交易ID
        List<OutgoingTransaction> outgoingTransactions = outgoingTransactionRepository
                .findByAccountIdAndDelFlagFalse(sourceAccount.getAccountId());
        Long transactionId = outgoingTransactions.get(0).getTransactionId();

        // 删除交易
        transactionService.deleteTransaction(transactionId);

        // 尝试修改已删除的交易
        assertThrows(EntityNotFoundException.class, () ->
                transactionService.modifyTransaction(transactionId, request));
    }

    @Test
    void listOutgoingTransactions_Success() {
        // 创建交易
        transactionService.createTransaction(request);

        // 查询转出交易
        List<OutgoingTransaction> transactions = transactionService.listOutgoingTransactions(sourceAccount.getAccountId());

        assertNotNull(transactions);
        assertEquals(1, transactions.size());
        assertEquals(request.getAmount(), transactions.get(0).getAmount());
    }

    @Test
    void listIncomingTransactions_Success() {
        // 创建交易
        transactionService.createTransaction(request);

        // 查询转入交易
        List<IncomingTransaction> transactions = transactionService.listIncomingTransactions(targetAccount.getAccountId());

        assertNotNull(transactions);
        assertEquals(1, transactions.size());
        assertEquals(request.getAmount(), transactions.get(0).getAmount());
    }
} 