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
    @KafkaListener(topics = "trade-deposit-succeeded", groupId = "market-service-group")
    public void listenDepositSucceededEvent(DepositSucceededPayloadDto event) {
        log.info("DepositSucceededEvent 수신: sellId={}", event.getSellId());
        tradeService.handleDepositSucceeded(event);
    }

    @KafkaListener(topics = "trade-deposit-failed", groupId = "market-service-group")
    public void listenDepositFailedEvent(DepositFailedPayloadDto event) {
        log.info("DepositFailedEvent 수신: sellId={}", event.getSellId());
        tradeService.handleDepositFailed(event);
    }

    @KafkaListener(topics = "trade-request-accepted", groupId = "market-service-group")
    public void listenTradeRequestAcceptedEvent(TradeRequestAcceptedPayloadDto event) {
        log.info("TradeRequestAcceptedEvent 수신: tradeId={}", event.getTradeId());
        tradeService.handleTradeRequestAccepted(event);
    }

    @KafkaListener(topics = "trade-request-rejected", groupId = "market-service-group")
    public void listenTradeRequestRejectedEvent(TradeRequestRejectedPayloadDto event) {
        log.info("TradeRequestRejectedEvent 수신: tradeId={}", event.getTradeId());
        tradeService.handleTradeRequestRejected(event);
    }

    @KafkaListener(topics = "trade-succeeded", groupId = "market-service-group")
    public void listenTradeSucceededEvent(TradeSucceededPayloadDto event) {
        log.info("TradeSucceededEvent 수신: tradeId={}", event.getTradeId());
        tradeService.handleTradeSucceeded(event);
    }

    @KafkaListener(topics = "trade-failed", groupId = "market-service-group")
    public void listenTradeFailedEvent(TradeFailedPayloadDto event) {
        log.info("TradeFailedEvent 수신: tradeId={}", event.getTradeId());
        tradeService.handleTradeFailed(event);
    }
}