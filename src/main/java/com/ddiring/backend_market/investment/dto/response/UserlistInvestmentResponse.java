package com.ddiring.backend_market.investment.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserlistInvestmentResponse {
    private Integer projectId;
    private Integer userSeq;
    private Integer tokenQuantity;
}
