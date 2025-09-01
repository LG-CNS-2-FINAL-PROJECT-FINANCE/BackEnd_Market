package com.ddiring.backend_market.api.asset.dto.request;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UpdateAssetRequestDto {
    private Long tradeId;
    private Long price; // 판매자에게 입금될 최종 금액

}