package com.ddiring.backend_market.trade.dto;

import com.ddiring.backend_market.trade.entity.Trade;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

//1. 프로젝트 번호
//2. 주문 번호
//3. 주문 유형
//4. 체결 금액
//5. 토큰 수량
//6. 체결 일자
@Getter
@NoArgsConstructor
public class TradeSearchResponseDto {
    private Integer ordersId;
    private Integer orderType;
    private Integer tradePrice;
    private Integer tokenQuantity;
    private LocalDateTime tradedAt;

    public TradeSearchResponseDto(Trade trade, Integer orderType) {
        this.ordersId = trade.getTradeId().intValue();// 1은 구매, 0은 판매
        this.orderType = orderType;
        this.tradePrice = trade.getTradePrice();
        this.tokenQuantity = trade.getTokenQuantity();
        this.tradedAt = trade.getTradedAt();
    }

}
