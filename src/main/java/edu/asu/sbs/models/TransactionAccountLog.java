package edu.asu.sbs.models;

import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;
import java.time.Instant;

@Data
@Entity
public class TransactionAccountLog implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long logNumber;

    private String logDescription;

    private Instant logTime;

    @OneToOne(mappedBy = "log")
    private Transaction transaction;

    @ManyToOne
    @JoinColumn(nullable = true)
    private Account account;
}
