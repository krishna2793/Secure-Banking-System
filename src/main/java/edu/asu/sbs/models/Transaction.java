package edu.asu.sbs.models;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import javax.persistence.*;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.Instant;

@Getter
@Setter
@Entity
public class Transaction implements Serializable {
    private static final long serialVersionUID = -1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long transactionId;

    @NotNull
    @Column(nullable = false, length = 50)
    private String status;

    @NotNull
    @Column(nullable = false)
    private String description;

    @NotNull
    @Column(nullable = false)
    private String transactionType;

    @CreatedDate
    private Instant createdTime;

    @NotNull
    @Column(nullable = false)
    @Min(1)
    private Double transactionAmount;

    @LastModifiedDate
    private Instant updatedTime;

    @ManyToOne
    @JoinColumn(nullable = false)
    private Account fromAccount;

    @ManyToOne
    @JoinColumn(nullable = false)
    private Account toAccount;

    @OneToOne
    @JoinColumn(nullable = false)
    private TransactionAccountLog log;

    @OneToOne(mappedBy = "linkedTransaction")
    private Request request;

    @OneToOne(mappedBy = "transaction")
    private Cheque cheque;

}
