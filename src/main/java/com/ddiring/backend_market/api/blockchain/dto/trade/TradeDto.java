package com.ddiring.backend_market.api.blockchain.dto.trade;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class TradeDto {
    private Integer tradeId;
    private String projectId;
    private BuyInfoDto buyInfo;
    private SellInfoDto sellInfo;
}
