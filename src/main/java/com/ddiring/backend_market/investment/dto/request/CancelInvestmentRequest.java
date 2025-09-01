package com.ddiring.backend_market.investment.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CancelInvestmentRequest {

    private Integer investmentSeq;
    private String projectId;
    private Integer investedPrice;
}
