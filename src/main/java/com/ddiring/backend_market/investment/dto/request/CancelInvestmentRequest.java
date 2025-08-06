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

    private Integer userSeq;        // 주문자 ID
    private Integer productId;      // 프로젝트 번호
    private Integer investmentSeq;  // 주문번호
}
