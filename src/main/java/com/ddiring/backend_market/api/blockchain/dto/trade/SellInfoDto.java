package com.ddiring.backend_market.api.blockchain.dto.trade;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SellInfoDto {
    private Long sellId;
    private String sellerAddress;
}
