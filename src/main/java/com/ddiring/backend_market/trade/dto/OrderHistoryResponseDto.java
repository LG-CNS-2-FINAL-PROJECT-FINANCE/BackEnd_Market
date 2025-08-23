package com.ddiring.backend_market.trade.dto;

import com.ddiring.backend_market.trade.entity.Trade;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

//1. 체결 금액
//2. 토큰 수량
//3. 체결 일자
@Getter
@NoArgsConstructor
public class OrderHistoryResponseDto {
    private Integer tradePrice;
    private Integer TokenQuantity;
    private LocalDateTime tradedAt;

    public OrderHistoryResponseDto(Trade trade) {
        this.tradePrice = trade.getTradePrice();
        this.TokenQuantity = trade.getTokenQuantity();
        this.tradedAt = trade.getTradedAt();
    }
}
