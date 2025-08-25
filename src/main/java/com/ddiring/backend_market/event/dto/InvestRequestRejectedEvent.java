package com.ddiring.backend_market.event.dto;

import lombok.*;
import java.time.Instant;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class InvestRequestRejectedEvent {
    public static final String EVENT_TYPE = "INVESTMENT.REQUEST.REJECTED";

    private String eventId;
    private String eventType; // INVESTMENT.REQUEST.REJECTED
    private Instant timestamp;
    private Payload payload;

    @Getter
    @Builder
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    @AllArgsConstructor
    public static class Payload {
        private Long investmentId;
        private String status; // PENDING
        private String reason;
    }
}
