package com.ddiring.backend_market.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AssetDTO {

    private Integer userSeq;
    private Integer deposit;
    private String projectId;
    private Integer tokenQuantity;
    private Integer investedPrice;
}
