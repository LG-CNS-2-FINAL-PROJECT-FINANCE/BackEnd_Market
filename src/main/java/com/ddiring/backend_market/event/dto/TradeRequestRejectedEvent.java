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
    public static final String TOPIC = "TRADE";

    private String eventId;
    private String eventType;
    private Instant timestamp;

    private TradeRequestRejectedPayload payload;

    @Getter
    @Builder
    @NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
    @AllArgsConstructor
    public static class TradeRequestRejectedPayload {
        private String projectId;
        private Long tradeId;
        private String buyerAddress;
        private String sellerAddress;
        private Long tradeAmount;
        private String status;
        private String errorMessage;
    }

    public static TradeRequestRejectedEvent of(String projectId, Long tradeId, String buyerAddress, String sellerAddress, Long tradeAmount, String errorMessage) {
        String uuid = java.util.UUID.randomUUID().toString();
        String eventType = TOPIC + ".REQUEST.REJECTED";

        return TradeRequestRejectedEvent.builder()
                .eventId(uuid)
                .eventType(eventType)
                .timestamp(Instant.now())
                .payload(TradeRequestRejectedPayload.builder()
                        .projectId(projectId)
                        .tradeId(tradeId)
                        .buyerAddress(buyerAddress)
                        .sellerAddress(sellerAddress)
                        .tradeAmount(tradeAmount)
                        .status("REJECTED")
                        .errorMessage(errorMessage)
                        .build()
                )
                .build();
    }
}