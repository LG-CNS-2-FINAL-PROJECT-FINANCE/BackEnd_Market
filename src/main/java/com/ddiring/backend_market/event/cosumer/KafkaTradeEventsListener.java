package com.ddiring.backend_market.event.cosumer;

import com.ddiring.backend_market.event.dto.*;
import com.ddiring.backend_market.trade.service.TradeService;
import com.fasterxml.jackson.databind.JsonNode;
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
    private final ObjectMapper objectMapper; // JSON 파싱을 위해 ObjectMapper 주입

    @KafkaListener(topics = "TRADE", groupId = "market-service-group")
    public void listenTradeEvents(Object message) {
        try {
            String jsonMessage;

            if (message instanceof String) {
                jsonMessage = (String) message;
            } else {
                jsonMessage = objectMapper.writeValueAsString(message);
            }

            JsonNode jsonNode = objectMapper.readTree(jsonMessage);
            String eventType = jsonNode.get("eventType").asText();
            log.info("수신된 이벤트 타입: {}", eventType);


            switch (eventType) {
                case "TRADE.DEPOSIT.SUCCEEDED":
                    DepositSucceededEvent depositSucceededEvent = objectMapper.readValue(jsonMessage, DepositSucceededEvent.class);
                    tradeService.handleDepositSucceeded(depositSucceededEvent);
                    break;
                case "TRADE.DEPOSIT.FAILED":
                    DepositFailedEvent depositFailedEvent = objectMapper.readValue(jsonMessage, DepositFailedEvent.class);
                    tradeService.handleDepositFailed(depositFailedEvent);
                    break;
                case "TRADE.REQUEST.ACCEPTED":
                    TradeRequestAcceptedEvent tradeRequestAcceptedEvent = objectMapper.readValue(jsonMessage, TradeRequestAcceptedEvent.class);
                    tradeService.handleTradeRequestAccepted(tradeRequestAcceptedEvent);
                    break;
                case "TRADE.REQUEST.REJECTED":
                    TradeRequestRejectedEvent tradeRequestRejectedEvent = objectMapper.readValue(jsonMessage, TradeRequestRejectedEvent.class);
                    tradeService.handleTradeRequestRejected(tradeRequestRejectedEvent);
                    break;
                case "TRADE.SUCCEEDED":
                    TradeSucceededEvent tradeSucceededEvent = objectMapper.readValue(jsonMessage, TradeSucceededEvent.class);
                    tradeService.handleTradeSucceeded(tradeSucceededEvent);
                    break;
                case "TRADE.FAILED":
                    TradeFailedEvent tradeFailedEvent = objectMapper.readValue(jsonMessage, TradeFailedEvent.class);
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