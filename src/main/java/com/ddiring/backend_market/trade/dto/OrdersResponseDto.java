package com.ddiring.backend_market.trade.dto;

import com.ddiring.backend_market.trade.entity.Orders;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

//1. 구매자ID
//2. 프로젝트 번호
//3. 구매희망가
//4. 주문수량
//5. 등록일시
@Getter
@NoArgsConstructor
public class OrdersResponseDto {
    private Integer userSeq;
    private String projectId;
    private Integer purchasePrice;
    private Integer tokenQuantity;
    private LocalDate registedAt;

    public OrdersResponseDto(Orders orders) {
        this.userSeq = orders.getUserSeq();
        this.projectId = orders.getProjectId();
        this.purchasePrice = orders.getPurchasePrice();
        this.tokenQuantity = orders.getTokenQuantity();
        this.registedAt = orders.getRegistedAt();
    }
}
