package com.demo.service;

import com.demo.dto.AccountDTO;
import com.demo.entities.*;
import com.demo.repository.AccountRepository;
import com.demo.repository.TransactionRepository;
import com.demo.repository.TransferRepository;
import com.demo.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private TransferRepository transferRepository;

    @InjectMocks
    private AccountService accountService;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    private User user;
    private Account account;
    private Transaction transaction;
    private Transfer transfer;

    @BeforeEach
    void setUp() {
        // Initialize objects
        user = new User();
        user.setId(1L);
        user.setUsername("testUser");

        account = new Account();
        account.setId(1L);
        account.setOwner(user);
        account.setAccountNumber("123456789012345");
        account.setAvailableBalance(1000.0);
        account.setTransactionHistory(new ArrayList<>());

        transaction = new Transaction();
        transaction.setId(1L);
        transaction.setAmount(500.0);
        transaction.setType(TransactionType.DEPOSIT);

        transfer = new Transfer();
        transfer.setAmount(200.0);

        // Mock security context setup
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn(user.getUsername());
    }

    @Test
    void createNewAccount_ShouldCreateAccount_WhenUserExists() {
        // Specific stubbing for this test
        when(userRepository.findByUsername("testUser")).thenReturn(Optional.of(user));
        when(accountRepository.save(any(Account.class))).thenReturn(account);

        AccountDTO accountDTO = accountService.createNewAccount(account, "testUser");

        assertAll(
                () -> assertNotNull(accountDTO),
                () -> assertEquals(account.getAccountNumber(), accountDTO.getAccountNumber()),
                () -> assertEquals(account.getAvailableBalance(), accountDTO.getAvailableBalance())
        );

        verify(accountRepository, times(1)).save(any(Account.class));
    }

    @Test
    void viewAllAccountsForUser_ShouldReturnAccountList_WhenAccountsExist() {
        when(userRepository.findByUsername("testUser")).thenReturn(Optional.of(user));
        when(accountRepository.findAllByOwner(user)).thenReturn(List.of(account));

        List<AccountDTO> accounts = accountService.viewAllAccountsForUser("testUser");

        assertAll(
                () -> assertNotNull(accounts),
                () -> assertEquals(1, accounts.size()),
                () -> assertEquals(account.getAccountNumber(), accounts.get(0).getAccountNumber())
        );

        verify(accountRepository, times(1)).findAllByOwner(user);
    }

    @Test
    void depositFunds_ShouldUpdateBalance_WhenTransactionValid() {
        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));
        when(transactionRepository.save(any(Transaction.class))).thenReturn(transaction);
        when(accountRepository.save(any(Account.class))).thenReturn(account);

        when(userRepository.findByUsername("testUser")).thenReturn(Optional.of(user));

        AccountDTO accountDTO = accountService.depositFunds(1L, transaction);

        assertAll(
                () -> assertNotNull(accountDTO),
                () -> assertEquals(1500.0, accountDTO.getAvailableBalance())
        );

        verify(transactionRepository, times(1)).save(any(Transaction.class));
        verify(accountRepository, times(1)).save(account);
    }


    @Test
    void withrawFunds_ShouldThrowException_WhenInsufficientFunds() {
        when(userRepository.findByUsername("testUser")).thenReturn(Optional.of(user));
        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));

        transaction.setAmount(2000.0);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            accountService.withrawFunds(1L, transaction);
        });

        assertEquals("Insufficient funds", exception.getMessage());
    }

    @Test
    void transferFunds_ShouldTransferFunds_WhenValid() {
        when(userRepository.findByUsername("testUser")).thenReturn(Optional.of(user));

        Account toAccount = new Account();
        toAccount.setId(2L);
        toAccount.setOwner(user);
        toAccount.setAvailableBalance(500.0);
        toAccount.setTransactionHistory(new ArrayList<>());

        account.setId(1L);
        account.setOwner(user);
        account.setAvailableBalance(1000.0);
        account.setTransactionHistory(new ArrayList<>());

        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));
        when(accountRepository.findById(2L)).thenReturn(Optional.of(toAccount));
        when(transferRepository.save(any(Transfer.class))).thenReturn(transfer);
        when(transactionRepository.save(any(Transaction.class))).thenReturn(transaction);
        when(accountRepository.save(any(Account.class))).thenReturn(account);

        AccountDTO accountDTO = accountService.transferFunds(1L, 2L, transfer);

        assertAll(
                () -> assertNotNull(accountDTO),
                () -> assertEquals(800.0, accountDTO.getAvailableBalance()),
                () -> assertEquals(700.0, toAccount.getAvailableBalance())
        );

        verify(transferRepository, times(1)).save(any(Transfer.class));
        verify(transactionRepository, times(1)).save(any(Transaction.class));
        verify(accountRepository, times(2)).save(any(Account.class));
    }



    @Test
    void validateOwner_ShouldThrowAccessDenied_WhenUserIsNotOwner() {
        when(userRepository.findByUsername("testUser")).thenReturn(Optional.of(user));

        User differentUser = new User();
        differentUser.setId(2L);
        differentUser.setUsername("differentUser");

        account.setId(1L);
        account.setOwner(differentUser);

        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            accountService.validateOwner(1L);
        });
        assertEquals("Unauthorized!", exception.getMessage());
    }


    @Test
    void deleteAccount_ShouldDeleteAccount_WhenAccountExists() {
        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));
        accountService.deleteAccount(1L);
        verify(accountRepository, times(1)).delete(account);
    }


    @Test
    void generateAccountNumber_ShouldGenerateValidAccountNumber() {
        String accountNumber = accountService.generateAccountNumber();

        assertAll(
                () -> assertNotNull(accountNumber),
                () -> assertEquals(15, accountNumber.length()),
                () -> assertTrue(accountNumber.matches("\\d{15}"))
        );
    }

    @Test
    void adminView_ShouldReturnAllAccounts_WhenAccountsExist() {
        when(accountRepository.findAll()).thenReturn(Arrays.asList(account));

        List<Account> accounts = accountService.adminView();

        assertAll(
                () -> assertNotNull(accounts),
                () -> assertFalse(accounts.isEmpty()),
                () -> assertEquals(1, accounts.size()),
                () -> assertTrue(accounts.get(0).getAccountNumber().contains("****"))
        );

        verify(accountRepository, times(1)).findAll();
    }
}
