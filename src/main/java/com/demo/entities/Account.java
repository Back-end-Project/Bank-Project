package com.demo.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
@Entity
@Table(name = "BANK_ACCOUNTS")
@Data
@NoArgsConstructor
public class Account {
    @Id
    @GeneratedValue
    private Long id;

    private String accountNumber;

    private Double availableBalance;

//    private List<Transaction> transactionHistory;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User owner;

}
