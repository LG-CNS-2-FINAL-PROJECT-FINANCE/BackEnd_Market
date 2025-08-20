package com.ddiring.backend_market.event.dto;// DepositDataDto.java
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class DepositEventDto {
    private String status;
    private String wallet;
    private Integer orderId;
    private Integer tokenQuantity;
}