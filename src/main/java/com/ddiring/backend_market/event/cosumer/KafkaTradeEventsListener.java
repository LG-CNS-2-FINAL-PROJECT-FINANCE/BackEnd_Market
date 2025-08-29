package com.ddiring.backend_market.event.cosumer;

import com.ddiring.backend_market.event.dto.*;
import com.ddiring.backend_market.trade.service.TradeService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaTradeEventsListener {

    private final TradeService tradeService;
    private final ObjectMapper objectMapper; // JSON 파싱을 위해 ObjectMapper 주입

    @KafkaListener(topics = "TRADE", groupId = "market-service-group")
    public void listenTradeEvents(String message) {
        try {

            Map<String, Object> messageMap = objectMapper.readValue(message, new TypeReference<>() {});

            String eventType = (String) messageMap.get("eventType");
            if (eventType == null) {
                log.warn("eventType 필드를 찾을 수 없습니다: {}", message);
                return;
            }

            log.info("수신된 이벤트 타입: {}", eventType);
            switch (eventType) {
                case "TRADE.REQUEST.ACCEPTED":
                    TradeRequestAcceptedEvent tradeRequestAcceptedEvent = objectMapper.convertValue(messageMap, TradeRequestAcceptedEvent.class);
                    log.info(tradeRequestAcceptedEvent.toString());
                    tradeService.handleTradeRequestAccepted(tradeRequestAcceptedEvent);
                    log.info(tradeRequestAcceptedEvent.toString());
                    break;
                case "TRADE.REQUEST.REJECTED":
                    TradeRequestRejectedEvent tradeRequestRejectedEvent = objectMapper.convertValue(messageMap, TradeRequestRejectedEvent.class);
                    log.info(tradeRequestRejectedEvent.toString());
                    tradeService.handleTradeRequestRejected(tradeRequestRejectedEvent);
                    log.info(tradeRequestRejectedEvent.toString());
                    break;
                case "TRADE.SUCCEEDED":
                    TradeSucceededEvent tradeSucceededEvent = objectMapper.convertValue(messageMap, TradeSucceededEvent.class);
                    log.info(tradeSucceededEvent.toString());
                    tradeService.handleTradeSucceeded(tradeSucceededEvent);
                    log.info(tradeSucceededEvent.toString());
                    break;
                case "TRADE.FAILED":
                    TradeFailedEvent tradeFailedEvent = objectMapper.convertValue(messageMap, TradeFailedEvent.class);
                    log.info(tradeFailedEvent.toString());
                    tradeService.handleTradeFailed(tradeFailedEvent);
                    log.info(tradeFailedEvent.toString());
                    break;
                default:
                    log.warn("알 수 없는 이벤트 타입입니다: {}", eventType);
                    break;
            }
        } catch (Exception e) {
            log.error("Kafka 메시지 처리 중 오류 발생: {}", message, e);
        }
    }
}