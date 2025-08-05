package com.ddiring.backend_market.investment.dto.request;

import lombok.*;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BuyInvestmentRequest {

    private Integer investmentSeq;
    private Integer userSeq;
    private Integer productId;
    private Integer tokenQuantity;
    private Integer investedPrice;
}
