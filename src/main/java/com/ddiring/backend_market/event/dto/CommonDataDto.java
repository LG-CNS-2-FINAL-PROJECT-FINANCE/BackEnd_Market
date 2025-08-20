package com.ddiring.backend_market.event.dto;// DepositCompletedEventDto.java
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class CommonDataDto {
    private String eventId;
    private String eventType;
    private LocalDateTime timestamp;
    private DepositEventDto payload;
}