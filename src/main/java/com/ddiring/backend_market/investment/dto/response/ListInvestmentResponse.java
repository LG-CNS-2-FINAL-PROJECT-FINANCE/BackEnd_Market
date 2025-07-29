package com.ddiring.backend_market.investment.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
// 투자 상품 전체 조회 DTO
public class ListInvestmentResponse {
    private Integer investmentSeq;
    private Integer productId;
    private String title;
    private int goal_amount;
    private LocalDate startDate;
    private LocalDate endDate;
}

