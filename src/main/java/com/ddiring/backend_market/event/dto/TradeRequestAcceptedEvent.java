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
    public static final String TOPIC = "TRADE";

    private String eventId;
    private String eventType;
    private Instant timestamp;

    private TradeRequestAcceptedPayload payload;

    @Getter
    @Builder
    @NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
    @AllArgsConstructor
    public static class TradeRequestAcceptedPayload {
        private String projectId;
        private Long tradeId;
        private String buyerAddress;
        private String sellerAddress;
        private Long tradeAmount;
        private String status;
    }

    public static TradeRequestAcceptedEvent of(String projectId, Long tradeId, String buyerAddress, String sellerAddress, Long tradeAmount) {
        String uuid = java.util.UUID.randomUUID().toString();
        String eventType = TOPIC + ".REQUEST.ACCEPTED";

        return TradeRequestAcceptedEvent.builder()
                .eventId(uuid)
                .eventType(eventType)
                .timestamp(Instant.now())
                .payload(TradeRequestAcceptedPayload.builder()
                        .projectId(projectId)
                        .tradeId(tradeId)
                        .status("ACCEPTED")
                        .buyerAddress(buyerAddress)
                        .sellerAddress(sellerAddress)
                        .tradeAmount(tradeAmount)
                        .build()
                )
                .build();
    }
}