package com.ddiring.backend_market.api.asset.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class MarketRefundDto {

    private Integer ordersId;
    private String projectId;
    private Integer refundPrice;
    private Integer refundAmount;
    private Integer orderType;
}
