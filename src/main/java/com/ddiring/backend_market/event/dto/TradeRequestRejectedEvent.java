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
public class TradeRequestRejectedEvent {
    public static final String TOPIC = "TRADE.REQUEST";

    private String eventId;
    private String eventType;
    private Instant timestamp;

    private TradeRequestRejectedPayload payload;

    @Getter
    @Builder
    @NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
    @AllArgsConstructor
    public static class TradeRequestRejectedPayload {
        private Long tradeId;
        private String status;
        private String errorMessage;
    }

    public static TradeRequestRejectedEvent of(Long tradeId, String errorMessage) {
        String uuid = java.util.UUID.randomUUID().toString();
        String eventType = TOPIC + ".REJECTED";

        return TradeRequestRejectedEvent.builder()
                .eventId(uuid)
                .eventType(eventType)
                .timestamp(Instant.now())
                .payload(TradeRequestRejectedPayload.builder()
                        .tradeId(tradeId)
                        .status("REJECTED")
                        .errorMessage(errorMessage)
                        .build()
                )
                .build();
    }
}