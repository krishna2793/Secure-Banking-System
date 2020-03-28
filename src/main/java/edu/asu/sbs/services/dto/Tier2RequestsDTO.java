package edu.asu.sbs.services.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.time.Instant;

@Data
public class Tier2RequestsDTO {

    @NotNull
    private Long transactionId;

    @NotNull
    private String status;

    @NotNull
    private String description;

    @NotNull
    private Long fromAccount;

    @NotNull
    private Long toAccount;

    @NotNull
    private Double amount;

    @NotNull
    private String type;

    @NotNull
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "UTC")
    private Instant createdDate;

    @NotNull
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "UTC")
    private Instant modifiedDate;


}
