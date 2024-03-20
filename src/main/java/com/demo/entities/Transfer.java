package com.demo.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
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

    private Long sourceAccount;

    private Long destinationAccount;
}
