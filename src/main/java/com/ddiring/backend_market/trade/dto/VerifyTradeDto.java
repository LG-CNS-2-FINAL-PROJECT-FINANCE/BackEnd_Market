package com.ddiring.backend_market.trade.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class VerifyTradeDto {
    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Request {
        @NotNull
        private Long tradeId;

        @NotNull
        private Integer buyId;

        @NotNull
        private Integer sellId;

        @NotNull
        private Integer tradeAmount;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Response {
        @NotNull
        private Boolean result;
    }
}
