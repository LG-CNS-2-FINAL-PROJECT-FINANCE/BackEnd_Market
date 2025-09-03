package com.ddiring.backend_market.event.dto;

import lombok.*;

import java.time.Instant;
import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
@AllArgsConstructor
public class InvestRequestAcceptedEvent {
    public static final String TOPIC = "INVESTMENT";

    // --- Header ---
    private String eventId;
    private String eventType;
    private Instant timestamp;

    // --- Payload ---
    private InvestRequestAcceptedPayload payload;

    @Getter
    @Builder
    @NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
    @AllArgsConstructor
    public static class InvestRequestAcceptedPayload {
        private String projectId;
        private Long investmentId;
        private String investorAddress;
        private Long tokenAmount;
        private String status;
    }

    public static InvestRequestAcceptedEvent of(String projectId, Long investmentId, String investorAddress, Long tokenAmount) {
        String uuid = java.util.UUID.randomUUID().toString();
        String eventType = TOPIC + ".REQUEST.ACCEPTED";

        return InvestRequestAcceptedEvent.builder()
                .eventId(uuid)
                .eventType(eventType)
                .timestamp(Instant.now())
                .payload(InvestRequestAcceptedPayload.builder()
                        .projectId(projectId)
                        .investmentId(investmentId)
                        .investorAddress(investorAddress)
                        .tokenAmount(tokenAmount)
                        .status("ACCEPTED")
                        .build()
                )
                .build();
    }
}