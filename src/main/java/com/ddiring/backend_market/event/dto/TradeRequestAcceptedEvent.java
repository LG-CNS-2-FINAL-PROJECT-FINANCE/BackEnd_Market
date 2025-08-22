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
public class TradeRequestAcceptedEvent {
    public static final String TOPIC = "TRADE.REQUEST";

    private String eventId;
    private String eventType;
    private Instant timestamp;

    private TradeRequestAcceptedPayload payload;

    @Getter
    @Builder
    @NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
    @AllArgsConstructor
    public static class TradeRequestAcceptedPayload {
        private Long tradeId;
        private String status;
    }

    public static TradeRequestAcceptedEvent of(Long tradeId) {
        String uuid = java.util.UUID.randomUUID().toString();
        String eventType = TOPIC + ".ACCEPTED";

        return TradeRequestAcceptedEvent.builder()
                .eventId(uuid)
                .eventType(eventType)
                .timestamp(Instant.now())
                .payload(TradeRequestAcceptedPayload.builder()
                        .tradeId(tradeId)
                        .status("ACCEPTED")
                        .build()
                )
                .build();
    }
}