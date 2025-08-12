package com.ddiring.backend_market.investment.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

// 주문 및 주문 취소
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InvestmentResponse {

    private Integer investmentSeq;
    private Integer userSeq;
    private String projectId;
    private Integer investedPrice;
    private Integer tokenQuantity;
    private String invStatus;
    private LocalDateTime investedAt;
}

