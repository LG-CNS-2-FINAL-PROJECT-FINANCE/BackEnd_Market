package com.ddiring.backend_market.api.asset.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor // Service 로직에서 편하게 생성하기 위해 추가
public class LockFundsRequestDto {
    private String userSeq;
    private String orderId;
    private Integer amount;
}