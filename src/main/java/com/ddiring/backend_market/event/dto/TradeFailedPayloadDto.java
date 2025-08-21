package com.ddiring.backend_market.event.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class TradeFailedPayloadDto {
    private Long tradeId;
    private String status;
    private String buyerAddress;
    private Long buyerTokenAmount;
    private String sellerAddress;
    private Long sellerTokenAmount;
    private String errorMessage;
}