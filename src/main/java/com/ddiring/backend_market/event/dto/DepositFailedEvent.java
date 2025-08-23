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
public class DepositFailedEvent {
    public static final String TOPIC = "DEPOSIT";

    private String eventId;
    private String eventType;
    private Instant timestamp;

    private DepositFailedPayload payload;

    @Getter
    @Builder
    @NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
    @AllArgsConstructor
    public static class DepositFailedPayload {
        private Long sellId;
        private String status;
        private String sellerAddress;
        private Long sellerTokenAmount;
        private String errorMessage;
    }

    public static DepositFailedEvent of(Long sellId, String sellerAddress, Long sellerTokenAmount, String errorMessage) {
        String uuid = java.util.UUID.randomUUID().toString();
        String eventType = TOPIC + ".FAILED";

        return DepositFailedEvent.builder()
                .eventId(uuid)
                .eventType(eventType)
                .timestamp(Instant.now())
                .payload(DepositFailedPayload.builder()
                        .sellId(sellId)
                        .status("FAILED")
                        .sellerAddress(sellerAddress)
                        .sellerTokenAmount(sellerTokenAmount)
                        .errorMessage(errorMessage)
                        .build()
                )
                .build();
    }
}