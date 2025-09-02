package com.ddiring.backend_market.event.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Getter
@Builder
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
@AllArgsConstructor
public class InvestSucceededEvent {
    public static final String PREFIX = "INVESTMENT";

    // --- Header ---
    private String eventId;
    private String eventType;
    private Instant timestamp;

    // --- Payload ---
    private InvestSucceededPayload payload;

    @Getter
    @Builder
    @NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
    @AllArgsConstructor
    public static class InvestSucceededPayload {
        private Long investmentId;
        private String projectId;
        private String status;
        private String investorAddress;
        private Long price;
        private Long tokenAmount;
    }

    public static InvestSucceededEvent of(Long investmentId, String projectId, String investorAddress,
            Long price, Long tokenAmount) {
        String uuid = java.util.UUID.randomUUID().toString();
        String eventType = PREFIX + ".SUCCEEDED";

        return InvestSucceededEvent.builder()
                .eventId(uuid)
                .eventType(eventType)
                .timestamp(Instant.now())
                .payload(InvestSucceededPayload.builder()
                        .investmentId(investmentId)
                        .projectId(projectId)
                        .status("SUCCEEDED")
                        .investorAddress(investorAddress)
                        .price(price)
                        .tokenAmount(tokenAmount)
                        .build())
                .build();
    }

    public static InvestSucceededEvent of(Integer investmentSeq, String projectId, String investorAddress, Long price,
            Integer tokenQuantity) {
        Long id = investmentSeq == null ? null : investmentSeq.longValue();
        Long tokenAmount = tokenQuantity == null ? null : tokenQuantity.longValue();
        return of(id, projectId, investorAddress, price, tokenAmount);
    }
}