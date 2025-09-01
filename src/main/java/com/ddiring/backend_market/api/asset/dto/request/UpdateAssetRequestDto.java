package com.ddiring.backend_market.api.asset.dto.request;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UpdateAssetRequestDto {
    private Long tradeId;
    private String projectId;
    private String buyAddress;
    private Long buyTokenAmount;
    private String sellAddress;
    private Long sellTokenAmount; // 판매자에게 입금될 최종 금액

}