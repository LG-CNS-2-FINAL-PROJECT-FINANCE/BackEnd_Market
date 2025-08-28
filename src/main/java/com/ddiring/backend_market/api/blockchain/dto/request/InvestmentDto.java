package com.ddiring.backend_market.api.blockchain.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InvestmentDto {
    private String projectId;
    private List<InvestInfo> investInfoList;

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class InvestInfo {
        private Long investmentId;
        private String investorAddress;
        private Long tokenAmount;
    }
}
