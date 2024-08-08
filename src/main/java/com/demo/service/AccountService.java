package com.demo.service;

import com.demo.dto.AccountDTO;
import com.demo.entities.Account;
import com.demo.entities.Transaction;
import com.demo.entities.TransactionType;
import com.demo.entities.User;
import com.demo.repository.AccountRepository;
import com.demo.repository.TransactionRepository;
import com.demo.entities.*;
import com.demo.repository.TransferRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import com.demo.repository.UserRepository;

import java.util.List;
import java.util.Random;

@Service
public class AccountService {
    private final AccountRepository accountRepository;
    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;
    private final TransferRepository transferRepository;

    @Autowired
    public AccountService(AccountRepository accountRepository, UserRepository userRepository, TransactionRepository transactionRepository, TransferRepository transferRepository) {
        this.accountRepository = accountRepository;
        this.userRepository = userRepository;
        this.transactionRepository = transactionRepository;
        this.transferRepository = transferRepository;
    }

    /**
     * Method to create a new account
     *
     * @param account  - the Request (could be null)
     * @param username - the uniq username of the user
     * @return - the newly created account
     */
    @Transactional
    public AccountDTO createNewAccount(Account account, String username) {
        User owner = userRepository.findByUsername(username).orElseThrow(() -> new RuntimeException("User not found:("));
        account.setOwner(owner);

        double accountBalance = getAccountBalance(account);
        account.setAvailableBalance(accountBalance);

        // Checking if the account number exists before assigning it
        String accountNumber = generateAccountNumber();
        if (accountRepository.findByAccountNumber(accountNumber).isPresent()) {
            String newAccountNumber = generateAccountNumber();
            account.setAccountNumber(newAccountNumber);
        } else {
            account.setAccountNumber(accountNumber);
        }

        account.setTransactionHistory(null);

        Account response = accountRepository.save(account);
        return mapAccountToDTO(response);
    }


    /**
     * Based on whoever is registered, the user will see only his accounts
     *
     * @param username - the uniq username of the user
     * @return - a list of accounts that belong to the user
     */
    public List<AccountDTO> viewAllAccountsForUser(String username) {
        User owner = userRepository.findByUsername(username).orElseThrow(() -> new RuntimeException("User not found:("));
        List<Account> accounts = accountRepository.findAllByOwner(owner);
        if (accounts.isEmpty()) {
            throw new RuntimeException("No accounts found for this user");
        }
        return accounts.stream().map(this::mapAccountToDTO).toList();
    }


    /**
     * Method to deposit funds into an account
     *
     * @param id          - the account to deposit funds into
     * @param transaction - the amount to deposit
     * @return - the updated account
     */
    public AccountDTO depositFunds(Long id, Transaction transaction)throws AccessDeniedException {
        validateOwner(id);
        Account account = accountRepository.findById(id).orElseThrow(() -> new RuntimeException("Account not found:("));
        double currentBalance = account.getAvailableBalance();
        double amount = transaction.getAmount();
        account.setAvailableBalance(currentBalance + amount);

        transaction.setType(TransactionType.DEPOSIT);
        transaction.setAccountId(account.getId());
        Transaction transactionFinish = transactionRepository.save(transaction);

        account.getTransactionHistory().add(transactionFinish);
        Account updatedAccount = accountRepository.save(account);

        return mapAccountToDTO(updatedAccount);
    }


    /**
     * Method to withdraw funds from an account
     *
     * @param id          - the account to withdraw funds from
     * @param transaction - the amount to withdraw
     * @return - the updated account
     */
    public AccountDTO withrawFunds(Long id, Transaction transaction) throws AccessDeniedException {
        validateOwner(id);
        Account account = accountRepository.findById(id).orElseThrow(() -> new RuntimeException("Account not found:("));
        double currentBalance = account.getAvailableBalance();
        double amount = transaction.getAmount();
        if (currentBalance < amount) {
            throw new RuntimeException("Insufficient funds");
        }
        account.setAvailableBalance(currentBalance - amount);
        transaction.setType(TransactionType.WITHDRAWAL);
        transaction.setAccountId(account.getId());
        Transaction transactionNew = transactionRepository.save(transaction);
        account.getTransactionHistory().add(transactionNew);
        Account response = accountRepository.save(account);
        return mapAccountToDTO(response);
    }

    /**
     * Method to transfer funds from one account to another
     *
     * @param fromAccountId - the account to transfer funds from
     *                      toAccountId - the account to transfer funds to
     *                      transaction - the amount to transfer
     * @return - the updated account
     */
    public AccountDTO transferFunds(Long fromAccountId, Long toAccountId, Transfer transfer) throws AccessDeniedException {
        validateOwner(fromAccountId);
        Account fromAccount = accountRepository.findById(fromAccountId).orElseThrow(() -> new RuntimeException("Account not found:("));
        Account toAccount = accountRepository.findById(toAccountId).orElseThrow(() -> new RuntimeException("Account not found:("));
        double currentBalance = fromAccount.getAvailableBalance();
        double amount = transfer.getAmount();
        if (currentBalance < amount) {
            throw new RuntimeException("Insufficient funds");
        }
        fromAccount.setAvailableBalance(currentBalance - amount);
        toAccount.setAvailableBalance(toAccount.getAvailableBalance() + amount);

        transfer.setSourceAccount(fromAccount.getId());
        transfer.setDestinationAccount(toAccount.getId());
        Transfer transferSaved = transferRepository.save(transfer);

        Transaction newTransaction = new Transaction();
        newTransaction.setAmount(amount);
        newTransaction.setType(TransactionType.TRANSFER);
        newTransaction.setAccountId(fromAccount.getId());
        newTransaction.setTransfer(transferSaved);
        Transaction transactionSaved = transactionRepository.save(newTransaction);

        toAccount.getTransactionHistory().add(transactionSaved);
        fromAccount.getTransactionHistory().add(transactionSaved);

        Account saveAcc = accountRepository.save(fromAccount);
        accountRepository.save(toAccount);
        return mapAccountToDTO(saveAcc);
    }


    /**
     * Method for Admin view only
     */
    public List<Account> adminView() {
        List<Account> accounts = accountRepository.findAll();
        if (accounts.isEmpty()) {
            throw new RuntimeException("No accounts found");
        }

        accounts.forEach(account -> account.setAccountNumber(HashMethod(account)));

        return accounts;
    }


    /**
     * Helper method to check if the user is the owner of the account
     */
    public void validateOwner(Long accountId) throws AccessDeniedException {
        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByUsername(currentUsername).orElseThrow(() -> new RuntimeException("User not found:("));
        Account account = accountRepository.findById(accountId).orElseThrow(() -> new RuntimeException("No Account found:("));
        if (!account.getOwner().equals(currentUser)) {
            throw new RuntimeException("Unauthorized!");
        }
    }


    /**
     * Helper method to generate an unique account number
     */
    public String generateAccountNumber() {
        Random random = new Random();
        StringBuilder builder = new StringBuilder();

        builder.append(random.nextInt(9) + 1);

        for (int i = 0; i < 14; i++) {
            builder.append(random.nextInt(10));
        }

        return builder.toString();
    }

    public void deleteAccount(Long id) {
        Account account = accountRepository.findById(id).orElseThrow(() -> new RuntimeException("Account not found:("));
        accountRepository.delete(account);
    }

    /**
     * Helper method to get/set the Account balance
     */
    public double getAccountBalance(Account account) {
        if (account.getAvailableBalance() != null) {
            return account.getAvailableBalance();
        } else {
            return 0.0;
        }
    }


    /**
     * Helper method to hash the account number
     */
    public String HashMethod(Account account) {
        String accountNumber = account.getAccountNumber();
        return accountNumber.substring(0, accountNumber.length() - 4).replaceAll("[0-9]", "*") + accountNumber.substring(accountNumber.length() - 4);
    }

    public AccountDTO mapAccountToDTO(Account account) {
        AccountDTO accountDTO = new AccountDTO();
        accountDTO.setId(account.getId());
        accountDTO.setAccountNumber(account.getAccountNumber());
        accountDTO.setAvailableBalance(account.getAvailableBalance());
        accountDTO.setTransactionHistory(account.getTransactionHistory());
        return accountDTO;
    }

}
