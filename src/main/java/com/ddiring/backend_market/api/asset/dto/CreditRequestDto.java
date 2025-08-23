package com.ddiring.backend_market.api.asset.dto;// com.ddiring.backend_market.api.dto.request.CreditRequestDto.java
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CreditRequestDto {
    private String userSeq;
    private Long amount; // 돈을 입금할 때
    private Integer quantity; // 토큰을 전송할 때
    private String tradeId;
}