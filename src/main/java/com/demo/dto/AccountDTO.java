package com.demo.dto;

import com.demo.entities.Transaction;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
@Getter
@Setter
@Data
public class AccountDTO {
    public Long id;
    public String accountNumber;
    public Double availableBalance;
    public List<Transaction> transactionHistory;

    public void setId(long l) {
    }
}
