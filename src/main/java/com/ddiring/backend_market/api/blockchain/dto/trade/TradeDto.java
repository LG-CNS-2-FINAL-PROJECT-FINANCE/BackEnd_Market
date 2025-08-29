package com.ddiring.backend_market.api.blockchain.dto.trade;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class TradeDto {
    private Long tradeId;
    private String projectId;
    private BuyInfoDto buyInfo;
    private SellInfoDto sellInfo;
    public TradeDto() {
        this.buyInfo = new BuyInfoDto();
        this.sellInfo = new SellInfoDto();
    }
}
