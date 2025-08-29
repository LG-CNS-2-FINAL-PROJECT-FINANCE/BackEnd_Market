package com.ddiring.backend_market.api.blockchain.dto.trade;

import lombok.Getter;

import lombok.Setter;

@Getter
@Setter
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
