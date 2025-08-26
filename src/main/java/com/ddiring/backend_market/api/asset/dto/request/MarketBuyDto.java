package com.ddiring.backend_market.api.asset.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class MarketBuyDto {
    public Integer buyPrice;
    public String projectId;
}
