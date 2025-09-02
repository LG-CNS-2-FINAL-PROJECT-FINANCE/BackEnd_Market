package com.ddiring.backend_market.investment.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InvestmentRequest {
    private Integer investedPrice;
    private Integer tokenQuantity;
}
