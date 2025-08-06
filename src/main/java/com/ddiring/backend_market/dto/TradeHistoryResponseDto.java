package com.ddiring.backend_market.dto;

import com.ddiring.backend_market.entity.Trade;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

//1. 체결 금액
//2. 토큰 수량
//3. 체결 일자
@Getter
@NoArgsConstructor
public class TradeHistoryResponseDto {
    private Integer tradePrice;
    private Integer TokenQuantity;
    private LocalDate tradedAt;

    public TradeHistoryResponseDto(Trade trade) {
        this.tradePrice = trade.getTradePrice();
        this.TokenQuantity = trade.getTokenQuantity();
        this.tradedAt = trade.getTradedAt();
    }
}
