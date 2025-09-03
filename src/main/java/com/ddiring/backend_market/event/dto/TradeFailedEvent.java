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
public class TradeFailedEvent {
    public static final String TOPIC = "TRADE";

    private String eventId;
    private String eventType;
    private Instant timestamp;

    private TradeFailedPayload payload;

    @Getter
    @Builder
    @NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
    @AllArgsConstructor
    public static class TradeFailedPayload {
        private String projectId;
        private Long tradeId;
        private String buyerAddress;
        private String sellerAddress;
        private Long tradeAmount;
        private String status;
        private String errorType;
        private String errorMessage;
    }

    public static TradeFailedEvent of(String projectId, Long tradeId, String buyerAddress, String sellerAddress, Long tradeAmount, String errorType, String errorMessage) {
        String uuid = java.util.UUID.randomUUID().toString();
        String eventType = TOPIC + ".FAILED";

        return TradeFailedEvent.builder()
                .eventId(uuid)
                .eventType(eventType)
                .timestamp(Instant.now())
                .payload(TradeFailedPayload.builder()
                        .projectId(projectId)
                        .tradeId(tradeId)
                        .status("FAILED")
                        .buyerAddress(buyerAddress)
                        .sellerAddress(sellerAddress)
                        .tradeAmount(tradeAmount)
                        .errorType(errorType)
                        .errorMessage(errorMessage)
                        .build()
                )
                .build();
    }
}