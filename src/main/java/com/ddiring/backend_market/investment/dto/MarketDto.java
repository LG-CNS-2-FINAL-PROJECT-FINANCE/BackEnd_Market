package com.ddiring.backend_market.investment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MarketDto {

    private String userSeq;
    private Integer price;
}
