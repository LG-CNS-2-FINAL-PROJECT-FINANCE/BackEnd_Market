package com.ddiring.backend_market.event.dto;

import lombok.*;
import java.time.Instant;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class InvestFailedEvent {
    public static final String EVENT_TYPE = "INVESTMENT.FAILED";

    private String eventId;
    private String eventType; // INVESTMENT.FAILED
    private Instant timestamp;
    private Payload payload;

    @Getter
    @Builder
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    @AllArgsConstructor
    public static class Payload {
        private Long investmentId;
        private String status; // FAILED
        private String errorType;
        private String errorMessage;
    }
}
