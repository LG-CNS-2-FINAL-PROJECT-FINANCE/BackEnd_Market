// AssetEscrowRequest.java
package com.ddiring.backend_market.api.asset.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AssetEscrowRequest {
    private Long tradeId;
    private String userSeq; // 구매자 또는 판매자의 userSeq
    private Long amount;
}