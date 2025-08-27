package com.ddiring.backend_market.api.asset.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class MarketSellDto {
    private Integer ordersId;
    private Integer sellToken;
    private String projectId;
}
