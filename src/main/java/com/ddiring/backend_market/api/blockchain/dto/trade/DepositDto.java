package com.ddiring.backend_market.api.blockchain.dto.trade;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigInteger;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
public class DepositDto {
    @NotBlank
    private String projectId;

    @NotNull
    private Long sellId;

    @NotBlank
    private String sellerAddress;

    @NotNull
    private BigInteger tokenAmount;

    @NotNull
    private BigInteger deadline;

    @NotNull
    private Integer v;

    @NotBlank
    private String r;

    @NotBlank
    private String s;
}