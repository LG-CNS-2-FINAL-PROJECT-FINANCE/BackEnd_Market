package com.ddiring.backend_market.api.asset.dto.response;


import com.ddiring.backend_market.api.asset.dto.request.BankSearchDto;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class BankBalanceResponse {
    private String code;
    private String message;
    private BankSearchDto data;
}