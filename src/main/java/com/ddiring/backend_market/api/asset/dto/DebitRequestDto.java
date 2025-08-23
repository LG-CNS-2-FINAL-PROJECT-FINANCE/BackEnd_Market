package com.ddiring.backend_market.api.asset.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class DebitRequestDto {
    private String userSeq;
    private Long amount; // 돈을 차감할 때
    private Integer quantity; // 토큰을 차감할 때
    private String tradeId;
}