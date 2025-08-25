package com.ddiring.backend_market.event.dto;

import lombok.*;
import java.time.Instant;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class InvestSucceededEvent {
    public static final String EVENT_TYPE = "INVESTMENT.SUCCEEDED";

    private String eventId;
    private String eventType; // INVESTMENT.SUCCEEDED
    private Instant timestamp;
    private Payload payload;

    @Getter
    @Builder
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    @AllArgsConstructor
    public static class Payload {
        private Long investmentId;
        private String status; // SUCCEEDED
        private String investorAddress;
        private Long tokenAmount;
        private String txHash;
    }
}
