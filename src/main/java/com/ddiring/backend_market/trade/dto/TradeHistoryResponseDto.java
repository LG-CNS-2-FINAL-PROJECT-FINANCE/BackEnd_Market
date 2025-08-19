package com.ddiring.backend_market.trade.dto;


import com.ddiring.backend_market.trade.entity.History;
import jakarta.persistence.Column;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
public class TradeHistoryResponseDto {
    private String projectId;
    private String userSeq;
    private Integer tradeType;
    private Integer tradePrice;
    private Integer tokenQuantity;
    private LocalDate tradedAt;

    public TradeHistoryResponseDto(History history) {
        this.projectId = history.getProjectId();
        this.userSeq = history.getUserSeq();
        this.tradeType = history.getTradeType();
        this.tradePrice = history.getTradePrice();
        this.tokenQuantity = history.getTokenQuantity();
        this.tradedAt = history.getTradedAt();
    }
}
