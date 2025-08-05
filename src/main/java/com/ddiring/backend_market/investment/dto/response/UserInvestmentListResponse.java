package com.ddiring.backend_market.investment.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

// 개인 투자 내역 조회
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserInvestmentListResponse {

    private Integer userSeq;
    private Integer projectId;
    private String title;
    private Integer investedPrice;
    private Integer tokenQuantity;
}
