package com.ddiring.backend_market.investment.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CancelInvestmentRequest {

    private Integer userSeq;
    private Integer projectId;
    private Integer investmentSeq;
}
