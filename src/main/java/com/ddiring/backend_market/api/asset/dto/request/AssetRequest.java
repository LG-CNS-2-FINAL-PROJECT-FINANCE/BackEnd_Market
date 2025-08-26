package com.ddiring.backend_market.api.asset.dto.request;

import com.ddiring.backend_market.api.product.ProductDTO;
import com.ddiring.backend_market.investment.dto.MarketDto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AssetRequest {
    private MarketDto marketDto;
    private ProductDTO productDto;
//    private Integer price;
//    private String projectId;
}
