package com.ddiring.backend_market.investment.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

// 투자 상품 전체 조회
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AllProductListResponse {

    private String projectId;
    private String title;
    private Integer currentAmount;
    private Integer achievementRate;
    private Integer deadline;
}
