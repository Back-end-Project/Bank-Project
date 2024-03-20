package com.demo.controller;
import com.demo.dto.AccountDTO;
import com.demo.entities.Account;
import com.demo.service.AccountService;
import org.hibernate.boot.model.source.spi.IdentifierSourceAggregatedComposite;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.List;

@RestController
public class UserController {
    private final AccountService accountService;

    public UserController(AccountService accountService) {
        this.accountService = accountService;
    }

    @GetMapping("/user_account")
    public ResponseEntity<List<AccountDTO>> userAccounts(Principal principal) {
        String username = principal.getName();
        List<AccountDTO> response = accountService.viewAllAccountsForUser(username);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }


    /**
     * This is the admin endpoint
     * The admin will see ALL the accounts
     * The sensitive data on the accounts will be hashed
     * Hashed account number - only last 4 digits will be visible - the others will be shown as ****
     * @return - A list of all the accounts
     */
    @GetMapping("/admin_only")
    public ResponseEntity<List<Account>> adminOnly() {
        List<Account> response = accountService.adminView();
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
    @DeleteMapping("/admin_only/delete/{accountId}")
    public ResponseEntity<String> deleteAccount(@PathVariable(value = "accountId") Long accountId){
        accountService.deleteAccount(accountId);
        return new ResponseEntity<>("Account deleted successfully!",HttpStatus.OK);
    }
}
