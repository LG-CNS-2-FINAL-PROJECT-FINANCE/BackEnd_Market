package com.ddiring.backend_market.investment.dto.request;

import com.ddiring.backend_market.investment.entity.Investment;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class AccountResponse {
    private Integer userSeq;
    private String account;

    public  AccountResponse(Investment investment) {
        this.userSeq = investment.getUserSeq();
        this.account = investment.getAccount();
    }
}
