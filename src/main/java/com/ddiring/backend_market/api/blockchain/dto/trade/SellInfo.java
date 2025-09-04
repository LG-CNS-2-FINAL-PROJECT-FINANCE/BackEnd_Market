package com.ddiring.backend_market.api.blockchain.dto.trade;

import lombok.*;


@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SellInfo {
    private Long sellId;
    private String sellerAddress;
}