package com.ddiring.backend_market.api.asset.dto.request;

import com.ddiring.backend_market.api.product.ProductDTO;
import com.ddiring.backend_market.investment.dto.MarketDto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AssetRequest {

    private MarketDto marketDto;
    private ProductDTO productDto;
}
