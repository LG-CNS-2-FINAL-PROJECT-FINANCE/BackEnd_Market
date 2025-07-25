package com.ddiring.backend_market.investment.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CancleInvestmentRequest {
    private Integer userSeq;
    private Integer projectId;
    private Integer InvestmentId;

//    public void CancleInvestment(Integer userSeq, Integer projectId, Integer InvestmentId) {
//        this.userSeq = userSeq;
//        this.projectId = projectId;
//        this.InvestmentId = InvestmentId;
//    }
}
