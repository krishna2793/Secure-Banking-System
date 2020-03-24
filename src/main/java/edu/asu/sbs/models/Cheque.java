package edu.asu.sbs.models;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import lombok.Data;

import javax.persistence.*;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Data
@Entity
public class Cheque implements Serializable {
    private static final long serialVersionUID = -1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long chequeId;

    @NotNull
    @Min(1)
    @Column(nullable = false)
    private Float amount;

    @OneToOne
    @JoinColumn(nullable = false)
    @JsonUnwrapped
    private Transaction transaction;

    private boolean isDeleted;

}
