package com.ddiring.backend_market.event.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class BaseEventDto<T> {
    private String eventId;
    private String eventType;
    private LocalDateTime timestamp;
    private T payload;
}