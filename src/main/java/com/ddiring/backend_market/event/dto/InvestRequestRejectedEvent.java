package com.ddiring.backend_market.event.dto;

import lombok.*;

import java.time.Instant;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class InvestRequestRejectedEvent {
    public static final String TOPIC = "INVESTMENT";

    // --- Header ---
    private String eventId;
    private String eventType;
    private Instant timestamp;

    // --- Payload ---
    private InvestRequestRejectedPayload payload;

    @Getter
    @Builder
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    @AllArgsConstructor
    public static class InvestRequestRejectedPayload {
        private String projectId;
        private Long investmentId;
        private String investorAddress;
        private Long tokenAmount;
        private String status;
        private String reason;
    }

    public static InvestRequestRejectedEvent of(String projectId, Long investmentId, String investorAddress, Long tokenAmount, String reason) {
        String uuid = java.util.UUID.randomUUID().toString();
        String eventType = TOPIC + ".REQUEST.REJECTED";

        return InvestRequestRejectedEvent.builder()
                .eventId(uuid)
                .eventType(eventType)
                .timestamp(Instant.now())
                .payload(InvestRequestRejectedPayload.builder()
                        .projectId(projectId)
                        .investmentId(investmentId)
                        .investorAddress(investorAddress)
                        .tokenAmount(tokenAmount)
                        .status("REJECTED")
                        .reason(reason)
                        .build()
                )
                .build();
    }
}