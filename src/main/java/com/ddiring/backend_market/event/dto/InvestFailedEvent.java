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
public class InvestFailedEvent {
    public static final String PREFIX = "INVESTMENT";

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
        private Long investmentId;
        private String status;
        private String errorType;
        private String errorMessage;
    }

    public static InvestFailedEvent of(Long investmentId, String errorType, String errorMessage) {
        String uuid = java.util.UUID.randomUUID().toString();
        String eventType = PREFIX + ".FAILED";

        return InvestFailedEvent.builder()
                .eventId(uuid)
                .eventType(eventType)
                .timestamp(Instant.now())
                .payload(InvestFailedPayload.builder()
                        .investmentId(investmentId)
                        .status("FAILED")
                        .errorType(errorType)
                        .errorMessage(errorMessage)
                        .build())
                .build();
    }

    public static InvestFailedEvent of(Integer investmentSeq, String errorType, String errorMessage) {
        Long id = investmentSeq == null ? null : investmentSeq.longValue();
        return of(id, errorType, errorMessage);
    }
}