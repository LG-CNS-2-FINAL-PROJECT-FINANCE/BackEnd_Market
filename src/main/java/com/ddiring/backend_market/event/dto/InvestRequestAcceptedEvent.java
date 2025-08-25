package com.ddiring.backend_market.event.dto;

import lombok.*;
import java.time.Instant;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class InvestRequestAcceptedEvent {
    public static final String EVENT_TYPE = "INVESTMENT.REQUEST.ACCEPTED";

    private String eventId;
    private String eventType; // INVESTMENT.REQUEST.ACCEPTED
    private Instant timestamp;
    private Payload payload;

    @Getter
    @Builder
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    @AllArgsConstructor
    public static class Payload {
        private Long investmentId; // investmentSeq
        private String status; // PENDING
    }
}
