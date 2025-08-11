package com.ddiring.backend_market.investment.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

// 프로젝트별 투자자 조회
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductInvestorResponse {

    private String projectId;
    private Integer totalInvestors;
    private List<InvestorInfo> investors;

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class InvestorInfo {
        private Integer userSeq;
        private Integer investedPrice;
        private Integer tokenQuantity;
    }
}
