package com.ddiring.backend_market.event.cosumer;// KafkaTradeEventsListener.java
import com.ddiring.backend_market.event.dto.*;
import com.ddiring.backend_market.trade.service.TradeService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaTradeEventsListener {

    private final TradeService tradeService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "trade-events-topic", groupId = "market-service-group")
    public void listenTradeEvents(String message) {
        log.info("Kafka로부터 거래 이벤트 수신: {}", message);

        try {
            BaseEventDto<?> event = objectMapper.readValue(message, BaseEventDto.class);
            String eventType = event.getEventType();

            switch (eventType) {
                case "DepositSucceeded":
                    BaseEventDto<DepositSucceededPayloadDto> successEvent = objectMapper.readValue(message, new com.fasterxml.jackson.core.type.TypeReference<BaseEventDto<DepositSucceededPayloadDto>>() {});
                    tradeService.handleDepositSucceeded(successEvent.getPayload());
                    break;
                case "DepositFailed":
                    BaseEventDto<DepositFailedPayloadDto> failedEvent = objectMapper.readValue(message, new com.fasterxml.jackson.core.type.TypeReference<BaseEventDto<DepositFailedPayloadDto>>() {});
                    tradeService.handleDepositFailed(failedEvent.getPayload());
                    break;
                case "TradeRequestAccepted":
                    BaseEventDto<TradeRequestAcceptedPayloadDto> acceptedEvent = objectMapper.readValue(message, new com.fasterxml.jackson.core.type.TypeReference<BaseEventDto<TradeRequestAcceptedPayloadDto>>() {});
                    tradeService.handleTradeRequestAccepted(acceptedEvent.getPayload());
                    break;
                case "TradeRequestRejected":
                    BaseEventDto<TradeRequestRejectedPayloadDto> rejectedEvent = objectMapper.readValue(message, new com.fasterxml.jackson.core.type.TypeReference<BaseEventDto<TradeRequestRejectedPayloadDto>>() {});
                    tradeService.handleTradeRequestRejected(rejectedEvent.getPayload());
                    break;
                case "TradeSucceeded":
                    BaseEventDto<TradeSucceededPayloadDto> tradeSuccessEvent = objectMapper.readValue(message, new com.fasterxml.jackson.core.type.TypeReference<BaseEventDto<TradeSucceededPayloadDto>>() {});
                    tradeService.handleTradeSucceeded(tradeSuccessEvent.getPayload());
                    break;
                case "TradeFailed":
                    BaseEventDto<TradeFailedPayloadDto> tradeFailedEvent = objectMapper.readValue(message, new com.fasterxml.jackson.core.type.TypeReference<BaseEventDto<TradeFailedPayloadDto>>() {});
                    tradeService.handleTradeFailed(tradeFailedEvent.getPayload());
                    break;
                default:
                    log.warn("알 수 없는 이벤트 타입 수신: {}", eventType);
                    break;
            }
        } catch (JsonProcessingException e) {
            log.error("이벤트 메시지 파싱 실패", e);
        }
    }
}