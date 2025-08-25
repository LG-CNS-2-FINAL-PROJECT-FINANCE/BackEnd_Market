package com.ddiring.backend_market.api.asset.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor // ⭐️ userSeq를 포함하는 생성자를 위해 추가
public class UnlockFundsRequestDto {
    private String orderId;
    private String userSeq; // ⭐️ 에셋 서비스와 양식을 맞추기 위해 이 필드가 추가되어야 합니다.
}