package com.ddiring.backend_market.api;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApiInvestmentDTO {

    private Integer userSeq;
    private Integer investedPrice;
}
