package com.ddiring.backend_market.event.dto;


import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class TradeRequestAcceptedPayloadDto {
    private Long tradeId;
}
