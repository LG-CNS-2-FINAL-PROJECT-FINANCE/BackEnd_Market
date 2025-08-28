package com.ddiring.backend_market.api.blockchain.dto.trade;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class SellInfoDto {
    private Long sellId;
    private String sellerAddress;
    private Long tokenAmount;
}
