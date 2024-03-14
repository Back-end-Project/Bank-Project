package com.demo.service;

import com.demo.entities.Account;
import com.demo.entities.User;
import com.demo.repository.AccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.demo.repository.UserRepository;

import java.util.Random;

@Service
public class AccountService {
    private final AccountRepository accountRepository;
    private final UserRepository userRepository;

    @Autowired
    public AccountService(AccountRepository accountRepository, UserRepository userRepository){
        this.accountRepository = accountRepository;
        this.userRepository = userRepository;
    }

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

//        account.setTransactionHistory(null);

        return accountRepository.save(account);
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
