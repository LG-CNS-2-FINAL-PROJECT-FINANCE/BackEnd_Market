package com.ddiring.backend_market.investment.dto.request;

import lombok.*;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BuyInvestmentRequest {
    private String account;
    private Integer investmentSeq;
    private Integer userSeq;
    private String projectId;
    private Integer tokenQuantity;
    private Integer investedPrice;
}
