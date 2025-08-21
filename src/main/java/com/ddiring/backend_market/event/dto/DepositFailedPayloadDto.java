package com.ddiring.backend_market.event.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class DepositFailedPayloadDto {
    private Long sellId;
    private String status;
    private String sellerAddress;
    private Long sellerTokenAmount;
    private String errorMessage;
}