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
// 투자 상품 상세 조회
public class DetailedInvestmentResponse {

    private Integer productId;
    private String title;
    private String content;
    private int goal_amount;
    private int min_investment;
    private int account;
    private LocalDate startDate;
    private LocalDate endDate;
}
