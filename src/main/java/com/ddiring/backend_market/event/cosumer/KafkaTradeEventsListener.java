package com.ddiring.backend_market.event.cosumer;

import com.ddiring.backend_market.event.dto.*;
import com.ddiring.backend_market.trade.service.TradeService;
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
    public void listenTradeEvents(Object message) {
        try {
            Map<String, Object> messageMap = (Map<String, Object>) message;

            // 2. ⭐️ Map에서 직접 eventType을 꺼냅니다.
            String eventType = (String) messageMap.get("eventType");
            if (eventType == null) {
                log.warn("eventType 필드를 찾을 수 없습니다: {}", message);
                return;
            }
            log.info("수신된 이벤트 타입: {}", eventType);

            // 3. ⭐️ objectMapper.convertValue를 사용해 Map을 원하는 DTO로 최종 변환합니다.
            switch (eventType) {
                case "TRADE.DEPOSIT.SUCCEEDED":
                    DepositSucceededEvent depositSucceededEvent = objectMapper.convertValue(messageMap, DepositSucceededEvent.class);
                    tradeService.handleDepositSucceeded(depositSucceededEvent);
                    break;
                case "TRADE.DEPOSIT.FAILED":
                    DepositFailedEvent depositFailedEvent = objectMapper.convertValue(messageMap, DepositFailedEvent.class);
                    tradeService.handleDepositFailed(depositFailedEvent);
                    break;
                case "TRADE.REQUEST.ACCEPTED":
                    TradeRequestAcceptedEvent tradeRequestAcceptedEvent = objectMapper.convertValue(messageMap, TradeRequestAcceptedEvent.class);
                    tradeService.handleTradeRequestAccepted(tradeRequestAcceptedEvent);
                    break;
                case "TRADE.REQUEST.REJECTED":
                    TradeRequestRejectedEvent tradeRequestRejectedEvent = objectMapper.convertValue(messageMap, TradeRequestRejectedEvent.class);
                    tradeService.handleTradeRequestRejected(tradeRequestRejectedEvent);
                    break;
                case "TRADE.SUCCEEDED":
                    TradeSucceededEvent tradeSucceededEvent = objectMapper.convertValue(messageMap, TradeSucceededEvent.class);
                    tradeService.handleTradeSucceeded(tradeSucceededEvent);
                    break;
                case "TRADE.FAILED":
                    TradeFailedEvent tradeFailedEvent = objectMapper.convertValue(messageMap, TradeFailedEvent.class);
                    tradeService.handleTradeFailed(tradeFailedEvent);
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