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
public class TradeSucceededEvent {
    public static final String TOPIC = "TRADE";

    private String eventId;
    private String eventType;
    private Instant timestamp;

    private TradeSucceededPayload payload;

    @Getter
    @Builder
    @NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
    @AllArgsConstructor
    public static class TradeSucceededPayload {
        private Long tradeId;
        private String status;
        private String buyerAddress;
        private Long buyerTokenAmount;
        private String sellerAddress;
        private Long sellerTokenAmount;
    }

    public static TradeSucceededEvent of(Long tradeId, String buyerAddress, Long buyerTokenAmount,
                                         String sellerAddress, Long sellerTokenAmount) {
        String uuid = java.util.UUID.randomUUID().toString();
        String eventType = TOPIC + ".SUCCEEDED";

        return TradeSucceededEvent.builder()
                .eventId(uuid)
                .eventType(eventType)
                .timestamp(Instant.now())
                .payload(TradeSucceededPayload.builder()
                        .tradeId(tradeId)
                        .status("SUCCEEDED")
                        .buyerAddress(buyerAddress)
                        .buyerTokenAmount(buyerTokenAmount)
                        .sellerAddress(sellerAddress)
                        .sellerTokenAmount(sellerTokenAmount)
                        .build()
                )
                .build();
    }
}