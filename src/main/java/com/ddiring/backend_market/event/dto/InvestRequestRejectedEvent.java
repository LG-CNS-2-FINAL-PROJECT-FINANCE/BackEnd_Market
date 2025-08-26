package com.ddiring.backend_market.event.dto;


import lombok.*;

import java.time.Instant;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class InvestRequestRejectedEvent {
    public static final String TOPIC = "INVESTMENT.REQUEST";

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
        private Long investmentId;
        private String status;
        private String reason;
    }

    public static InvestRequestRejectedEvent of(Long investmentId, String reason) {
        String uuid = java.util.UUID.randomUUID().toString();
        String eventType = TOPIC + ".REJECTED";

        return InvestRequestRejectedEvent.builder()
                .eventId(uuid)
                .eventType(eventType)
                .timestamp(Instant.now())
                .payload(InvestRequestRejectedPayload.builder()
                        .investmentId(investmentId)
                        .status("REJECTED")
                        .reason(reason)
                        .build()
                )
                .build();
    }
}