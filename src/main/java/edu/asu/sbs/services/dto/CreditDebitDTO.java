package edu.asu.sbs.services.dto;

import edu.asu.sbs.globals.CreditDebitType;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class CreditDebitDTO {
    @NotNull
    private Double amount;
    @NotNull
    private CreditDebitType creditDebitType;
    @NotNull
    private String description;
    @NotNull
    private Long id;
}
