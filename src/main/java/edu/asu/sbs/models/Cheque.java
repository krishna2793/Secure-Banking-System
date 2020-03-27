package edu.asu.sbs.models;

import com.fasterxml.jackson.annotation.JsonBackReference;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Getter
@Setter
@Entity
public class Cheque implements Serializable {
    private static final long serialVersionUID = -1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long chequeId;

    @NotNull
    @Min(1)
    @Column(nullable = false)
    private Double amount;

    @JsonBackReference
    @OneToOne
    @JoinColumn(nullable = false)
    private Transaction transaction;

    @JsonBackReference
    @ManyToOne
    @JoinColumn(nullable = false)
    private Account chequeFromAccount;

    @JsonBackReference
    @ManyToOne
    @JoinColumn(nullable = false)
    private Account chequeToAccount;

    private boolean isDeleted;

}
