package com.demo.service;

import com.demo.entities.Account;
import com.demo.entities.Transaction;
import com.demo.entities.TransactionType;
import com.demo.entities.User;
import com.demo.repository.AccountRepository;
import com.demo.repository.TransactionRepository;
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

    @Autowired
    public AccountService(AccountRepository accountRepository, UserRepository userRepository, TransactionRepository transactionRepository){
        this.accountRepository = accountRepository;
        this.userRepository = userRepository;
        this.transactionRepository = transactionRepository;
    }

    /**
     * Method to create a new account
     * @param account - the Request (could be null)
     * @param username - the uniq username of the user
     * @return - the newly created account
     */
    public Account createNewAccount(Account account, String username){
        User owner = userRepository.findByUsername(username).orElseThrow(()->new RuntimeException("User not found:("));
        account.setOwner(owner);

        double accountBalance = getAccountBalance(account);
        account.setAvailableBalance(accountBalance);

        // Checking if the account number exists before assigning it
        String accountNumber = generateAccountNumber();
        if(accountRepository.findByAccountNumber(accountNumber).isPresent()){
            String newAccountNumber = generateAccountNumber();
            account.setAccountNumber(newAccountNumber);
        } else{
            account.setAccountNumber(accountNumber);
        }

        account.setTransactionHistory(null);

        return accountRepository.save(account);
    }

    /**
     * Based on whoever is registered, the user will see only his accounts
     * @param username - the uniq username of the user
     * @return - a list of accounts that belong to the user
     */
    public List<Account> viewAllAccountsForUser(String username){
        User owner = userRepository.findByUsername(username).orElseThrow(()->new RuntimeException("User not found:("));
        List<Account> accounts = accountRepository.findAllByOwner(owner);
        if(accounts.isEmpty()){
            throw new RuntimeException("No accounts found for this user");
        }
        return accounts;
    }


    /**
     * Method to deposit funds into an account
     * @param id - the account to deposit funds into
     * @param transaction - the amount to deposit
     * @return - the updated account
     */
    public Account depositFunds(Long id, Transaction transaction) throws AccessDeniedException {
        Account account = accountRepository.findById(id).orElseThrow(()->new RuntimeException("Account not found:("));
        validateOwner(id);
        double currentBalance = account.getAvailableBalance();
        double amount = transaction.getAmount();
        account.setAvailableBalance(currentBalance + amount);
        transaction.setType(TransactionType.DEPOSIT);
        transaction.getAccountID().add(account);
        transactionRepository.save(transaction);
        account.getTransactionHistory().add(transaction);
        return account;
    }


    /**
     * Method to withdraw funds from an account
     * @param id - the account to withdraw funds from
     * @param transaction - the amount to withdraw
     * @return - the updated account
     */
    public Account withrawFunds(Long id, Transaction transaction) throws AccessDeniedException{
        Account account = accountRepository.findById(id).orElseThrow(()->new RuntimeException("Account not found:("));
        validateOwner(id);
        double currentBalance = account.getAvailableBalance();
        double amount = transaction.getAmount();
        if(currentBalance < amount){
            throw new RuntimeException("Insufficient funds");
        }
        account.setAvailableBalance(currentBalance - amount);
        transaction.setType(TransactionType.WITHDRAWAL);
        transactionRepository.save(transaction);
        account.getTransactionHistory().add(transaction);
        return accountRepository.save(account);
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
     *  Helper method to generate an unique account number
     */
    public String generateAccountNumber(){
        Random random = new Random();
        StringBuilder builder = new StringBuilder();

        builder.append(random.nextInt(9) + 1);

        for(int i = 0; i < 14; i++){
            builder.append(random.nextInt(10));
        }

        return builder.toString();
    }


    /**
     * Helper method to get/set the Account balance
     */
    public double getAccountBalance(Account account){
        if(account.getAvailableBalance() != null){
            return account.getAvailableBalance();
        } else {
            return 0.0;
        }
    }
}

