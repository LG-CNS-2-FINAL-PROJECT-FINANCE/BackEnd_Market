package com.ddiring.backend_market.investment.dto.request;

import com.ddiring.backend_market.investment.entity.Investment;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BuyInvestmentRequest {
    private Integer userSeq;
    private Integer projectId;
    private Integer tokenQuantity;

//    public void BuyInvestment(Integer userSeq, Integer projectId, Integer tokenQuantity) {
//        this.userSeq = userSeq;
//        this.projectId = projectId;
//        this.tokenQuantity = tokenQuantity;
//    }

//    public Investment toEntity() {
//        Investment investment = new Investment();
//
//    }
}
