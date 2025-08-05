package com.ddiring.backend_market.investment.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

// 투자 상품 전체 조회
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AllProductListResponse {

    private Integer projectId;
    private String title;
    private Integer currentAmount; // 현재 투자 금액
    private Integer achievementRate; // 달성률
    private LocalDate startDate;
    private LocalDate endDate;
    private String status;
}
