package edu.asu.sbs.services.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class RequestDTO {

    @NotNull
    private String requestType;

    @NotNull
    private String description;

}
