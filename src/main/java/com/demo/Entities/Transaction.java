package com.demo.Entities;

import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;

import java.util.List;

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
     * If this is the wright approach for the accountID
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id")
    private Account accountID;

    private List<Transfer> transferHistory;

    /**
     * TO BE ADDED:
     * Time stamp
     */
}
