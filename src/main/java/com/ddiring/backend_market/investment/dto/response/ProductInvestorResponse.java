package com.ddiring.backend_market.investment.dto.response;

import com.ddiring.backend_market.api.user.UserDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

// 프로젝트별 투자자 조회
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductInvestorResponse {

    private UserDTO user;
    private Integer investedPrice;
    private Integer tokenQuantity;
    private LocalDateTime investedAt;
}
