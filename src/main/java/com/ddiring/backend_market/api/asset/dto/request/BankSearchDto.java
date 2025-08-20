package com.ddiring.backend_market.api.asset.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class BankSearchDto {
    public String bankNumber;
    public Integer deposit;
}
