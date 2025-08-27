package com.ddiring.backend_market.investment.dto.response;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CanceledInvestmentResponse {

    private Integer investedPrice;
    private LocalDateTime investedAt;
    private LocalDateTime updatedAt;
}
