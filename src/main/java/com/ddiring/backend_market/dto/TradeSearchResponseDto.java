package com.ddiring.backend_market.dto;

import com.ddiring.backend_market.entity.Trade;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

//1. 프로젝트 번호
//2. 주문 번호
//3. 주문 유형
//4. 체결 금액
//5. 토큰 수량
//6. 체결 일자
@Getter
@NoArgsConstructor
public class TradeSearchResponseDto {
    private String projectId; // 프로젝트 번호
    private Integer ordersId; // 주문 번호 (purchase_id 또는 sell_id에서 유추)
    private Integer orderType; // 주문 유형 (0: 판매, 1: 구매)
    private Integer tradePrice; // 체결 금액
    private Integer tokenQuantity; // 토큰 수량
    private LocalDate tradedAt; // 체결 일자

    public TradeSearchResponseDto(Trade trade, Integer orderType) {
        this.projectId = trade.getProjectId();
        this.ordersId = orderType == 1 ? trade.getPurchaseId() : trade.getSellId(); // 1은 구매, 0은 판매
        this.orderType = orderType;
        this.tradePrice = trade.getTradePrice();
        this.tokenQuantity = trade.getTokenQuantity();
        this.tradedAt = trade.getTradedAt();
    }

}
