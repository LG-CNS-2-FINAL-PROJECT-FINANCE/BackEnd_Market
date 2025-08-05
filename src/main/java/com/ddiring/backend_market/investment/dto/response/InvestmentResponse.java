package com.ddiring.backend_market.investment.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.util.List;

// 주문 및 주문 취소
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InvestmentResponse {

    private Integer tokenQuantity;
}

