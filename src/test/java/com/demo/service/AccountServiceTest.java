package com.demo.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.demo.dto.AccountDTO;
import com.demo.entities.Account;
import com.demo.entities.Transaction;
import com.demo.entities.Transfer;
import com.demo.entities.User;
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
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
public class AccountServiceTest {

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

    private User user;
    private Account account;
    private Transaction transaction;
    private Transfer transfer;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setUsername("testuser");

        account = new Account();
        account.setId(1L);
        account.setOwner(user);
        account.setAvailableBalance(1000.0);
        account.setTransactionHistory(Collections.emptyList());

        transaction = new Transaction();
        transaction.setAmount(100.0);

        transfer = new Transfer();
        transfer.setAmount(100.0);
    }

    @Test
    void createNewAccount_Success() {
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(user));
        when(accountRepository.save(any(Account.class))).thenReturn(account);
        when(accountRepository.findByAccountNumber(anyString())).thenReturn(Optional.empty());

        AccountDTO result = accountService.createNewAccount(account, "testuser");

        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(accountRepository, times(1)).save(any(Account.class));
    }

    @Test
    void createNewAccount_UserNotFound() {
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());

        Exception exception = assertThrows(RuntimeException.class, () -> {
            accountService.createNewAccount(account, "unknownuser");
        });

        assertEquals("User not found:(", exception.getMessage());
    }

    @Test
    void createNewAccount_NullAccount() {
        Exception exception = assertThrows(RuntimeException.class, () -> {
            accountService.createNewAccount(null, "testuser");
        });

        assertNull(exception.getMessage());
    }

    @Test
    void viewAllAccountsForUser_Success() {
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(user));
        when(accountRepository.findAllByOwner(any(User.class))).thenReturn(Arrays.asList(account));

        List<AccountDTO> result = accountService.viewAllAccountsForUser("testuser");

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(1L, result.get(0).getId());
    }

    @Test
    void viewAllAccountsForUser_UserNotFound() {
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());

        Exception exception = assertThrows(RuntimeException.class, () -> {
            accountService.viewAllAccountsForUser("unknownuser");
        });

        assertEquals("User not found:(", exception.getMessage());
    }

    @Test
    void viewAllAccountsForUser_NoAccountsFound() {
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(user));
        when(accountRepository.findAllByOwner(any(User.class))).thenReturn(Collections.emptyList());

        Exception exception = assertThrows(RuntimeException.class, () -> {
            accountService.viewAllAccountsForUser("testuser");
        });

        assertEquals("No accounts found for this user", exception.getMessage());
    }

    @Test
    void depositFunds_Success() throws AccessDeniedException {
        when(accountRepository.findById(anyLong())).thenReturn(Optional.of(account));
        when(transactionRepository.save(any(Transaction.class))).thenReturn(transaction);
        doNothing().when(accountService).validateOwner(anyLong());

        AccountDTO result = accountService.depositFunds(1L, transaction);

        assertNotNull(result);
        assertEquals(1100.0, result.getAvailableBalance());
        verify(accountRepository, times(1)).save(any(Account.class));
    }

    @Test
    void depositFunds_AccountNotFound() {
        when(accountRepository.findById(anyLong())).thenReturn(Optional.empty());
        doNothing().when(accountService).validateOwner(anyLong());

        Exception exception = assertThrows(RuntimeException.class, () -> {
            accountService.depositFunds(1L, transaction);
        });

        assertEquals("Account not found:(", exception.getMessage());
    }

    @Test
    void depositFunds_NullTransaction() {
        Exception exception = assertThrows(RuntimeException.class, () -> {
            accountService.depositFunds(1L, null);
        });

        assertNull(exception.getMessage());
    }

    @Test
    void withdrawFunds_Success() throws AccessDeniedException {
        when(accountRepository.findById(anyLong())).thenReturn(Optional.of(account));
        when(transactionRepository.save(any(Transaction.class))).thenReturn(transaction);
        doNothing().when(accountService).validateOwner(anyLong());

        AccountDTO result = accountService.withrawFunds(1L, transaction);

        assertNotNull(result);
        assertEquals(900.0, result.getAvailableBalance());
        verify(accountRepository, times(1)).save(any(Account.class));
    }

    @Test
    void withdrawFunds_InsufficientFunds() {
        account.setAvailableBalance(50.0);
        when(accountRepository.findById(anyLong())).thenReturn(Optional.of(account));
        doNothing().when(accountService).validateOwner(anyLong());

        Exception exception = assertThrows(RuntimeException.class, () -> {
            accountService.withrawFunds(1L, transaction);
        });

        assertEquals("Insufficient funds", exception.getMessage());
    }

    @Test
    void withdrawFunds_NullTransaction() {
        Exception exception = assertThrows(RuntimeException.class, () -> {
            accountService.withrawFunds(1L, null);
        });

        assertNull(exception.getMessage());
    }

    @Test
    void transferFunds_Success() throws AccessDeniedException {
        Account toAccount = new Account();
        toAccount.setId(2L);
        toAccount.setAvailableBalance(500.0);
        toAccount.setTransactionHistory(Collections.emptyList());

        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));
        when(accountRepository.findById(2L)).thenReturn(Optional.of(toAccount));
        when(transferRepository.save(any(Transfer.class))).thenReturn(transfer);
        when(transactionRepository.save(any(Transaction.class))).thenReturn(transaction);
        doNothing().when(accountService).validateOwner(1L);

        AccountDTO result = accountService.transferFunds(1L, 2L, transfer);

        assertNotNull(result);
        assertEquals(900.0, result.getAvailableBalance());
        verify(accountRepository, times(2)).save(any(Account.class));
    }

    @Test
    void transferFunds_InsufficientFunds() throws AccessDeniedException {
        // Arrange
        account.setAvailableBalance(50.0);
        Account toAccount = new Account();
        toAccount.setId(2L);
        toAccount.setAvailableBalance(500.0);

        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));
        when(accountRepository.findById(2L)).thenReturn(Optional.of(toAccount));
        doNothing().when(accountService).validateOwner(1L);

        Exception exception = assertThrows(RuntimeException.class, () -> {
            accountService.transferFunds(1L, 2L, transfer);
        });

        assertEquals("Insufficient funds", exception.getMessage());
    }


    @Test
    void transferFunds_NullTransfer() {
        Exception exception = assertThrows(RuntimeException.class, () -> {
            accountService.transferFunds(1L, 2L, null);
        });

        assertNull(exception.getMessage());
    }

    @Test
    void adminView_Success() {
        when(accountRepository.findAll()).thenReturn(Arrays.asList(account));

        List<Account> result = accountService.adminView();

        assertNotNull(result);
        assertFalse(result.isEmpty());
        verify(accountRepository, times(1)).findAll();
    }

    @Test
    void adminView_NoAccountsFound() {
        when(accountRepository.findAll()).thenReturn(Collections.emptyList());

        Exception exception = assertThrows(RuntimeException.class, () -> {
            accountService.adminView();
        });

        assertEquals("No accounts found", exception.getMessage());
    }

    @Test
    void adminView_AccountsEmptyList() {
        when(accountRepository.findAll()).thenReturn(null);

        Exception exception = assertThrows(RuntimeException.class, () -> {
            accountService.adminView();
        });

        assertNull(exception.getMessage());
    }

    @Test
    void deleteAccount_Success() {
        when(accountRepository.findById(anyLong())).thenReturn(Optional.of(account));
        doNothing().when(accountRepository).delete(any(Account.class));

        accountService.deleteAccount(1L);

        verify(accountRepository, times(1)).delete(any(Account.class));
    }

    @Test
    void deleteAccount_AccountNotFound() {
        when(accountRepository.findById(anyLong())).thenReturn(Optional.empty());

        Exception exception = assertThrows(RuntimeException.class, () -> {
            accountService.deleteAccount(1L);
        });

        assertEquals("Account not found:(", exception.getMessage());
    }

    @Test
    void deleteAccount_NullId() {
        Exception exception = assertThrows(RuntimeException.class, () -> {
            accountService.deleteAccount(null);
        });

        assertNull(exception.getMessage());
    }

    @Test
    void validateOwner_Success() {
        Authentication authentication = mock(Authentication.class);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        when(authentication.getName()).thenReturn("testuser");
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(user));
        when(accountRepository.findById(anyLong())).thenReturn(Optional.of(account));

        assertDoesNotThrow(() -> {
            accountService.validateOwner(1L);
        });
    }

    @Test
    void validateOwner_Unauthorized() {
        User anotherUser = new User();
        anotherUser.setUsername("anotheruser");
        account.setOwner(anotherUser);

        Authentication authentication = mock(Authentication.class);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        when(authentication.getName()).thenReturn("testuser");
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(user));
        when(accountRepository.findById(anyLong())).thenReturn(Optional.of(account));

        Exception exception = assertThrows(RuntimeException.class, () -> {
            accountService.validateOwner(1L);
        });

        assertEquals("Unauthorized!", exception.getMessage());
    }

    @Test
    void validateOwner_UserNotFound() {
        Authentication authentication = mock(Authentication.class);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        when(authentication.getName()).thenReturn("testuser");
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());

        Exception exception = assertThrows(RuntimeException.class, () -> {
            accountService.validateOwner(1L);
        });

        assertEquals("User not found:(", exception.getMessage());
    }

    @Test
    void generateAccountNumber_Success() {
        String accountNumber = accountService.generateAccountNumber();

        assertNotNull(accountNumber);
        assertEquals(15, accountNumber.length());
    }

    @Test
    void generateAccountNumber_Null() {
        when(accountRepository.findByAccountNumber(anyString())).thenReturn(Optional.empty());

        assertDoesNotThrow(() -> {
            accountService.generateAccountNumber();
        });
    }

    @Test
    void getAccountBalance_AccountNotNull() {
        double balance = accountService.getAccountBalance(account);

        assertEquals(1000.0, balance);
    }

    @Test
    void getAccountBalance_AccountIsNull() {
        double balance = accountService.getAccountBalance(null);

        assertEquals(0.0, balance);
    }

    @Test
    void hashMethod_Success() {
        String hashedNumber = accountService.HashMethod(account);

        assertNotNull(hashedNumber);
        assertTrue(hashedNumber.startsWith("***********"));
    }

    @Test
    void hashMethod_EmptyAccountNumber() {
        account.setAccountNumber("");

        String hashedNumber = accountService.HashMethod(account);

        assertNotNull(hashedNumber);
        assertEquals("", hashedNumber);
    }

    @Test
    void mapAccountToDTO_Success() {
        AccountDTO accountDTO = accountService.mapAccountToDTO(account);

        assertNotNull(accountDTO);
        assertEquals(1L, accountDTO.getId());
        assertEquals(1000.0, accountDTO.getAvailableBalance());
    }

    @Test
    void mapAccountToDTO_NullAccount() {
        AccountDTO accountDTO = accountService.mapAccountToDTO(null);

        assertNull(accountDTO);
    }
}
