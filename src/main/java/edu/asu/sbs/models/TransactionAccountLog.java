package edu.asu.sbs.models;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
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

    @JsonManagedReference
    @OneToOne(mappedBy = "log")
    private Transaction transaction;

    @JsonBackReference
    @ManyToOne
    @JoinColumn
    private Account account;
}
