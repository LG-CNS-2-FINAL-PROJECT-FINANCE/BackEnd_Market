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
    public static final String TOPIC = "INVESTMENT";

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
        private String projectId;
        private Long investmentId;
        private String status;
        private String investorAddress;
        private Long tokenAmount;
        private Long initialAmountPerToken;
    }

    public static InvestSucceededEvent of(String projectId, Long investmentId, String investorAddress, Long tokenAmount,
            Long initialAmountPerToken) {
        String uuid = java.util.UUID.randomUUID().toString();
        String eventType = TOPIC + ".SUCCEEDED";

        return InvestSucceededEvent.builder()
                .eventId(uuid)
                .eventType(eventType)
                .timestamp(Instant.now())
                .payload(InvestSucceededPayload.builder()
                        .projectId(projectId)
                        .investmentId(investmentId)
                        .status("SUCCEEDED")
                        .investorAddress(investorAddress)
                        .tokenAmount(tokenAmount)
                        .initialAmountPerToken(initialAmountPerToken)
                        .build())
                .build();
    }
}