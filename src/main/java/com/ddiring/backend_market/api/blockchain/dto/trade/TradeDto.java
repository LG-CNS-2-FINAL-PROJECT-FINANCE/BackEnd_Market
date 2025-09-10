package com.ddiring.backend_market.api.blockchain.dto.trade;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TradeDto {
    @NotNull(message = "거래 번호는 필수입니다.")
    @Positive(message = "거래 번호는 양수여야 합니다.")
    private Long tradeId;

    @NotBlank(message = "프로젝트 아이디는 필수입니다.")
    private String projectId;

    @NotNull
    private BuyInfo buyInfo;

    @NotNull
    private SellInfo sellInfo;

    @NotNull
    private Long tradeAmount;

    @NotNull
    private Long pricePerToken;
}
