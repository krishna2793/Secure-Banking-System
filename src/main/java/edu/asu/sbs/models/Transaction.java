package edu.asu.sbs.models;

import lombok.Data;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.sql.Timestamp;

@Data
@Entity
@Table(name = "Transaction")
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long transactionId;

    @NotNull
    @Column(name = "status", nullable = false, length = 50)
    private String status;

    @NotNull
    private String description;

    @NotNull
    private String transactionType;
    private Timestamp createdTime;
    private Double transactionAmount;
    private Timestamp updatedTime;

    @ManyToOne
    @JoinColumn(name="FkFromAccount", nullable=false)
    private Account fromAccount;

    @ManyToOne
    @JoinColumn(name="FkToAccount", nullable=false)
    private Account toAccount;

}
