package com.ddiring.backend_market.event.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
@AllArgsConstructor
public class InvestFailedEvent {
    public static final String TOPIC = "INVESTMENT";

    // --- Header ---
    private String eventId;
    private String eventType;
    private Instant timestamp;

    // --- Payload ---
    private InvestFailedPayload payload;

    @Getter
    @Builder
    @NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
    @AllArgsConstructor
    public static class InvestFailedPayload {
        private String projectId;
        private Long investmentId;
        private String investorAddress;
        private Long tokenAmount;
        private String status;
        private String errorType;
        private String errorMessage;
    }

    public static InvestFailedEvent of(String projectId, Long investmentId, String investorAddress, Long tokenAmount, String errorType, String errorMessage) {
        String uuid = java.util.UUID.randomUUID().toString();
        String eventType = TOPIC + ".FAILED";

        return InvestFailedEvent.builder()
                .eventId(uuid)
                .eventType(eventType)
                .timestamp(Instant.now())
                .payload(InvestFailedPayload.builder()
                        .projectId(projectId)
                        .investmentId(investmentId)
                        .investorAddress(investorAddress)
                        .tokenAmount(tokenAmount)
                        .status("FAILED")
                        .errorType(errorType)
                        .errorMessage(errorMessage)
                        .build()
                )
                .build();
    }
}