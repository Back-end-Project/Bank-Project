package com.demo.controller;

import com.demo.dto.AccountDTO;
import com.demo.entities.Account;
import com.demo.entities.Transaction;
import com.demo.entities.Transfer;
import com.demo.service.AccountService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.*;
import com.demo.service.AccountService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;

import java.security.Principal;

@RestController
@RequestMapping("/account")
public class AccountController {
    private final AccountService accountService;

    public AccountController(AccountService accountService){
        this.accountService = accountService;
    }

    @PostMapping("/create")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<Account> createAccount(@RequestBody Account request, Principal principal){
        String username = principal.getName();
        Account response = accountService.createNewAccount(request, username);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PutMapping("/{accountId}/deposit")
    public ResponseEntity<AccountDTO> deposit(@PathVariable(value = "accountId") Long accountId, @RequestBody Transaction request) {
        AccountDTO response = accountService.depositFunds(accountId, request);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PutMapping("/{accountId}/withdraw")
    public ResponseEntity<AccountDTO> withdraw(@PathVariable(value = "accountId") Long accountId, @RequestBody Transaction request) {
        AccountDTO response = accountService.withrawFunds(accountId, request);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
    @PutMapping("/transfer/{fromAccountId}/{toAccountId}")
    public ResponseEntity<Account> transferFund(@PathVariable(value = "fromAccountId")Long fromAccountId,@PathVariable(value = "toAccountId") Long toAccountId, @RequestBody Transfer request ) throws AccessDeniedException{
        Account response = accountService.transferFunds(fromAccountId, toAccountId, request);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

}
