package edu.asu.sbs.models;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;
import java.time.Instant;

@Getter
@Setter
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
    @JoinColumn
    private Account account;
}
