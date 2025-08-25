package com.ddiring.backend_market.event.dto;

import lombok.*;

import java.time.Instant;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class InvestRequestEvent {
    public static final String EVENT_TYPE = "INVESTMENT.REQUEST";

    private String eventId;
    private String eventType; // INVESTMENT.REQUEST
    private Instant timestamp;
    private Payload payload;

    @Getter
    @Builder
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    @AllArgsConstructor
    public static class Payload {
        private String projectId;
        private List<InvestmentItem> investments;
    }

    @Getter
    @Builder
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    @AllArgsConstructor
    public static class InvestmentItem {
        private Integer investmentSeq;
        private String userSeq;
        private Integer investedPrice;
        private Integer tokenQuantity;
    }

    public static InvestRequestEvent of(String projectId, List<InvestmentItem> items) {
        return InvestRequestEvent.builder()
                .eventId(java.util.UUID.randomUUID().toString())
                .eventType(EVENT_TYPE)
                .timestamp(Instant.now())
                .payload(Payload.builder()
                        .projectId(projectId)
                        .investments(items)
                        .build())
                .build();
    }
}
