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
public class DepositSucceededEvent {
    public static final String TOPIC = "DEPOSIT";

    private String eventId;
    private String eventType;
    private Instant timestamp;

    private DepositSucceededPayload payload;

    @Getter
    @Builder
    @NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
    @AllArgsConstructor
    public static class DepositSucceededPayload {
        private String projectId;
        private Long sellId;
        private String status;
        private String sellerAddress;
        private Long sellerTokenAmount;
    }

    public static DepositSucceededEvent of(String projectId, Long sellId, String sellerAddress, Long sellerTokenAmount) {
        String uuid = java.util.UUID.randomUUID().toString();
        String eventType = TOPIC + ".SUCCEEDED";

        return DepositSucceededEvent.builder()
                .eventId(uuid)
                .eventType(eventType)
                .timestamp(Instant.now())
                .payload(DepositSucceededPayload.builder()
                        .projectId(projectId)
                        .sellId(sellId)
                        .status("SUCCEEDED")
                        .sellerAddress(sellerAddress)
                        .sellerTokenAmount(sellerTokenAmount)
                        .build()
                )
                .build();
    }
}