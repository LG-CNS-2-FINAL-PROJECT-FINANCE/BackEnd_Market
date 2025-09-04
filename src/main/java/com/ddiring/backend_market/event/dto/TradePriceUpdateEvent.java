package com.ddiring.backend_market.event.dto;

import lombok.*;

import java.time.Instant;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class TradePriceUpdateEvent {
    public static final String TOPIC = "TRADE";
    public static final String EVENT_TYPE = "TRADE.PRICE.UPDATED";

    private String eventId;
    private String eventType;
    private Instant timestamp;
    private Payload payload;

    @Getter
    @Builder
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    @AllArgsConstructor
    public static class Payload {
        private String projectId;
        private Integer pricePerToken; // 토큰당 체결 가격
    }

    public static TradePriceUpdateEvent of(String projectId, Integer pricePerToken) {
        return TradePriceUpdateEvent.builder()
                .eventId(java.util.UUID.randomUUID().toString())
                .eventType(EVENT_TYPE)
                .timestamp(Instant.now())
                .payload(Payload.builder()
                        .projectId(projectId)
                        .pricePerToken(pricePerToken)
                        .build())
                .build();
    }
}