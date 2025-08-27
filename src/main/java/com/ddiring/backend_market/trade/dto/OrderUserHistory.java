package com.ddiring.backend_market.trade.dto;

import com.ddiring.backend_market.trade.entity.Orders;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class OrderUserHistory {
    private Integer orderId;
    private String projectId;
    private Integer purchasePrice;
    private Integer tokenQuantity;
    private Integer ordersType;
    public  OrderUserHistory(Orders order) {
        this.orderId = order.getOrdersId();
        this.projectId = order.getProjectId();
        this.purchasePrice = order.getPurchasePrice();
        this.tokenQuantity = order.getTokenQuantity();
        this.ordersType = order.getOrdersType();
    }
}
