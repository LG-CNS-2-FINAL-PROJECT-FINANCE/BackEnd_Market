package com.ddiring.backend_market.investment.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MarketDto {
    private String productId;
    private String userSeq;
    private Integer price;
}
