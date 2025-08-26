package com.ddiring.backend_market.api.asset.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class MarketSellDto {
    private Integer sellToken;
    private String projectId;
}
