package com.ddiring.backend_market.investment.dto.response;

import java.time.LocalDateTime;

import com.ddiring.backend_market.investment.entity.Investment.InvestmentStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MyInvestmentByProductResponse {

    private Integer investmentSeq;
    private Integer investedPrice;
    private Integer tokenQuantity;
    private LocalDateTime investedAt;
    private InvestmentStatus invStatus;
}
