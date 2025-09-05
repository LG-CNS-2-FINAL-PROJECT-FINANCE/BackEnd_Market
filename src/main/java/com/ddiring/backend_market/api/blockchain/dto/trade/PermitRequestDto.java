package com.ddiring.backend_market.api.blockchain.dto.trade;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PermitRequestDto {
    private String projectId;
    private String userAddress;
    private Long tokenAmount;
}