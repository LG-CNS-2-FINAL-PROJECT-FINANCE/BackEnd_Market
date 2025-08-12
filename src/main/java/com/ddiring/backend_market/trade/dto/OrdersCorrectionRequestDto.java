package com.ddiring.backend_market.trade.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

//1. 구매자ID
//2. 프로젝트 번호
//3. 구매 희망가
//4. 주문수량
@Getter
@NoArgsConstructor
public class OrdersCorrectionRequestDto extends OrdersRequestDto {
    private Integer ordersId;
}
