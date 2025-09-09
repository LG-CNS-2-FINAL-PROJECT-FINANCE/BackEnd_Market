package com.ddiring.backend_market.trade.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TradeInfoResponseDto {
    private Long tradeId;
    private String projectId;
    private Integer price;
    private Integer tokenQuantity;
    private Integer buyerUserSeq;
    private Integer sellerUserSeq;
    private String status;
}