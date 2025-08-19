package com.ddiring.backend_market.trade.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

//1. 판매자ID
//2. 프로젝트 번호
//3. 판매희망가
//4. 주문수량
@Getter
@NoArgsConstructor
public class OrdersRequestDto {
    private String userSeq;
    private String projectId;
    private Integer purchasePrice;
    private Integer tokenQuantity;
}
