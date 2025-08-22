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
        private Long tradeId;
        private String status;
        private String buyerAddress;
        private Long buyerTokenAmount;
        private String sellerAddress;
        private Long sellerTokenAmount;
        private String errorMessage;
    }

    public static TradeFailedEvent of(Long tradeId, String buyerAddress, Long buyerTokenAmount,
                                      String sellerAddress, Long sellerTokenAmount, String errorMessage) {
        String uuid = java.util.UUID.randomUUID().toString();
        String eventType = TOPIC + ".FAILED";

        return TradeFailedEvent.builder()
                .eventId(uuid)
                .eventType(eventType)
                .timestamp(Instant.now())
                .payload(TradeFailedPayload.builder()
                        .tradeId(tradeId)
                        .status("FAILED")
                        .buyerAddress(buyerAddress)
                        .buyerTokenAmount(buyerTokenAmount)
                        .sellerAddress(sellerAddress)
                        .sellerTokenAmount(sellerTokenAmount)
                        .errorMessage(errorMessage)
                        .build()
                )
                .build();
    }
}