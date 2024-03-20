package com.demo.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "BANK_TRANSFERS")
@Data
@NoArgsConstructor
public class Transfer {
    @Id
    @GeneratedValue
    private Long id;

    private Double amount;

    /**
     * Will have to decide if this will be a type Long or Account
     * 1. If it is a Long, then we will have to query the database to get the account
     * 2. If it is an Account, we will have to pass in the whole account into the Body Request
     */

//    private Long transactionId;

    private Long sourceAccount;

    private Long destinationAccount;
}
