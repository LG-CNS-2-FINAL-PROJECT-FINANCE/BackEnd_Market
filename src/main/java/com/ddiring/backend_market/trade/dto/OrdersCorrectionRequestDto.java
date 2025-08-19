package com.ddiring.backend_market.trade.dto;

import com.ddiring.backend_market.trade.entity.History;
import jakarta.persistence.Column;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class OrdersCorrectionRequestDto extends OrdersRequestDto {
    private Integer ordersId;
}
