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
        private Long investmentId;
        private String status;
        private String investorAddress;
        private Long tokenAmount;
    }

    public static InvestSucceededEvent of(Long investmentId, String investorAddress, Long tokenAmount) {
        String uuid = java.util.UUID.randomUUID().toString();
        String eventType = TOPIC + ".SUCCEEDED";

        return InvestSucceededEvent.builder()
                .eventId(uuid)
                .eventType(eventType)
                .timestamp(Instant.now())
                .payload(InvestSucceededPayload.builder()
                        .investmentId(investmentId)
                        .status("SUCCEEDED")
                        .investorAddress(investorAddress)
                        .tokenAmount(tokenAmount)
                        .build()
                )
                .build();
    }
}