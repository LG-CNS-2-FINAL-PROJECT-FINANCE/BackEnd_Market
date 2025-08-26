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

//    private final TradeService tradeService;
//    private final ObjectMapper objectMapper; // JSON 파싱을 위해 ObjectMapper 주입
//
//    @KafkaListener(topics = "TRADE", groupId = "market-service-group")
//    public void listenTradeEvents(String message) {
//        try {
//            JsonNode jsonNode = objectMapper.readTree(message);
//            String eventType = jsonNode.get("eventType").asText();
//            log.info("수신된 이벤트 타입: {}", eventType);
//
//            switch (eventType) {
//                case "TRADE.DEPOSIT.SUCCEEDED":
//                    DepositSucceededEvent depositSucceededEvent = objectMapper.readValue(message, DepositSucceededEvent.class);
//                    tradeService.handleDepositSucceeded(depositSucceededEvent);
//                    break;
//                case "TRADE.DEPOSIT.FAILED":
//                    DepositFailedEvent depositFailedEvent = objectMapper.readValue(message, DepositFailedEvent.class);
//                    tradeService.handleDepositFailed(depositFailedEvent);
//                    break;
//                case "TRADE.REQUEST.ACCEPTED":
//                    TradeRequestAcceptedEvent tradeRequestAcceptedEvent = objectMapper.readValue(message, TradeRequestAcceptedEvent.class);
//                    tradeService.handleTradeRequestAccepted(tradeRequestAcceptedEvent);
//                    break;
//                case "TRADE.REQUEST.REJECTED":
//                    TradeRequestRejectedEvent tradeRequestRejectedEvent = objectMapper.readValue(message, TradeRequestRejectedEvent.class);
//                    tradeService.handleTradeRequestRejected(tradeRequestRejectedEvent);
//                    break;
//                case "TRADE.SUCCEEDED":
//                    TradeSucceededEvent tradeSucceededEvent = objectMapper.readValue(message, TradeSucceededEvent.class);
//                    tradeService.handleTradeSucceeded(tradeSucceededEvent);
//                    break;
//                case "TRADE.FAILED":
//                    TradeFailedEvent tradeFailedEvent = objectMapper.readValue(message, TradeFailedEvent.class);
//                    tradeService.handleTradeFailed(tradeFailedEvent);
//                    break;
//                default:
//                    log.warn("알 수 없는 이벤트 타입입니다: {}", eventType);
//                    break;
//            }
//        } catch (Exception e) {
//            log.error("Kafka 메시지 처리 중 오류 발생: {}", message, e);
//        }
//    }
}