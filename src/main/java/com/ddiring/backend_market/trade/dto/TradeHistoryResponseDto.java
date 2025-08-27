package com.ddiring.backend_market.trade.dto;


import com.ddiring.backend_market.trade.entity.History;
import jakarta.persistence.Column;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class TradeHistoryResponseDto {
    private Integer tradeType;
    private Integer tradePrice;
    private Integer tokenQuantity;
    private LocalDateTime tradedAt;

    public TradeHistoryResponseDto(History history) {
        this.tradeType = history.getTradeType();
        this.tradePrice = history.getTradePrice();
        this.tokenQuantity = history.getTokenQuantity();
        this.tradedAt = history.getTradedAt();
    }
}
