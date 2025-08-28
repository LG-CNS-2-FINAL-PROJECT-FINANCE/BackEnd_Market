package com.ddiring.backend_market.api.blockchain.dto.trade;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class BuyInfoDto {
    private Long buyId;
    private String buyerAddress;
    private Long tokenAmount;
}
