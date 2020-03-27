package edu.asu.sbs.services.dto;

import edu.asu.sbs.models.Request;
import edu.asu.sbs.models.TransactionAccountLog;
import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

@Data
public class TransactionDTO {

    private Long transactionId;

    private String status;

    @NotNull
    private String description;

    @NotNull
    private String transactionType;

    @Min(1)
    @NotNull
    private Double transactionAmount;

    @NotNull
    private Long fromAccount;

    @NotNull
    private Long toAccount;

    private Request request;

    private TransactionAccountLog log;

}
