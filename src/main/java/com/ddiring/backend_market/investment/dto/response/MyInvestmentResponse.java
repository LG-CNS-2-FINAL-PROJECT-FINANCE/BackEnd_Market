package com.ddiring.backend_market.investment.dto.response;

import com.ddiring.backend_market.api.product.ProductDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

// 개인 투자 내역 조회
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MyInvestmentResponse {

    private ProductDTO product;
    private Integer investedPrice;
    private Integer tokenQuantity;
}
