package com.ddiring.backend_market.event.dto;

import lombok.*;

import java.time.Instant;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class AllocationRequestedEvent {
    public static final String EVENT_TYPE = "INVEST.ALLOC.REQUESTED";

    private String eventId;
    private String eventType; // INVEST.ALLOC.REQUESTED
    private Instant timestamp;
    private Payload payload;

    @Getter
    @Builder
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    @AllArgsConstructor
    public static class Payload {
        private String projectId;
        private Long totalInvested; // sum price
        private Long totalTokens; // sum token quantity
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

    public static AllocationRequestedEvent of(String projectId, List<InvestmentItem> items) {
        long totalInvested = items.stream().mapToLong(i -> i.getInvestedPrice()).sum();
        long totalTokens = items.stream().mapToLong(i -> i.getTokenQuantity()).sum();
        return AllocationRequestedEvent.builder()
                .eventId(java.util.UUID.randomUUID().toString())
                .eventType(EVENT_TYPE)
                .timestamp(Instant.now())
                .payload(Payload.builder()
                        .projectId(projectId)
                        .totalInvested(totalInvested)
                        .totalTokens(totalTokens)
                        .investments(items)
                        .build())
                .build();
    }
}
