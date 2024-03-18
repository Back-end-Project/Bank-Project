package com.demo.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;

import java.util.List;

@Entity
@Table(name = "BANK_TRANSACTIONS")
@Data
public class Transaction {
    @Id
    @GeneratedValue
    private Long id;

    private TransactionType type;

    private Double amount;

    /**
     * TO BE decided:
     * If this is the right approach for the accountID
     */
//    @ManyToMany(mappedBy = "transactionHistory", fetch = FetchType.LAZY)
//    private List<Account> accountID;

//    private List<Transfer> transferHistory;

    /**
     * TO BE ADDED:
     * Time stamp
     */
}
