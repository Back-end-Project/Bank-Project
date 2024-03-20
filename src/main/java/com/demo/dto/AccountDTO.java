package com.demo.dto;

import com.demo.entities.Transaction;
import lombok.Data;

import java.util.List;

@Data
public class AccountDTO {
    public Long id;
    public String accountNumber;
    public Double availableBalance;
    public List<Transaction> transactionHistory;
}
