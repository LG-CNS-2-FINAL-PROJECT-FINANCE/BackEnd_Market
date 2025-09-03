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
        private String projectId;
        private Long tradeId;
        private String status;
        private String buyerAddress;
        private String sellerAddress;
        private Long tradeAmount;
    }

    public static TradeSucceededEvent of(String projectId, Long tradeId, String buyerAddress, String sellerAddress, Long tradeAmount) {
        String uuid = java.util.UUID.randomUUID().toString();
        String eventType = TOPIC + ".SUCCEEDED";

        return TradeSucceededEvent.builder()
                .eventId(uuid)
                .eventType(eventType)
                .timestamp(Instant.now())
                .payload(TradeSucceededPayload.builder()
                        .projectId(projectId)
                        .tradeId(tradeId)
                        .status("SUCCEEDED")
                        .buyerAddress(buyerAddress)
                        .sellerAddress(sellerAddress)
                        .tradeAmount(tradeAmount)
                        .build()
                )
                .build();
    }
}