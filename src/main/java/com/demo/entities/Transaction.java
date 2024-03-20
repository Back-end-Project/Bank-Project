package com.demo.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Data;

import java.sql.Timestamp;

@Entity
@Table(name = "BANK_TRANSACTIONS")
@Data
public class Transaction {
    @Id
    @GeneratedValue
    private Long id;

    private TransactionType type;

    private Double amount;

    public Long accountId;

    @OneToOne(fetch = FetchType.LAZY)
    public Transfer transfer;

    public Timestamp timestamp = new Timestamp(System.currentTimeMillis());

}

