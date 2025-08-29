package com.ddiring.backend_market.api.blockchain.dto.trade;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BuyInfoDto {
    private Long buyId;
    private String buyerAddress;
    private Long tokenAmount;
}
