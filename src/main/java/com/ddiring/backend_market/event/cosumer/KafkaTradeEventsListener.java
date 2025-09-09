package com.ddiring.backend_market.event.cosumer;

import com.ddiring.backend_market.api.asset.AssetClient;
import com.ddiring.backend_market.api.asset.dto.request.AssetEscrowRequest;
import com.ddiring.backend_market.api.asset.dto.request.UpdateAssetRequestDto;
import com.ddiring.backend_market.event.dto.*;
import com.ddiring.backend_market.trade.entity.Orders;
import com.ddiring.backend_market.trade.entity.Trade;
import com.ddiring.backend_market.trade.repository.OrdersRepository;
import com.ddiring.backend_market.trade.repository.TradeRepository;
import com.ddiring.backend_market.trade.service.TradeService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaTradeEventsListener {

    private final TradeRepository tradeRepository;
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
                    handleTradeRequestAccepted(tradeRequestAcceptedEvent);
                    log.info(tradeRequestAcceptedEvent.toString());
                    break;
                case "TRADE.REQUEST.REJECTED":
                    TradeRequestRejectedEvent tradeRequestRejectedEvent = objectMapper.convertValue(messageMap, TradeRequestRejectedEvent.class);
                    log.info(tradeRequestRejectedEvent.toString());
                    handleTradeRequestRejected(tradeRequestRejectedEvent);
                    log.info(tradeRequestRejectedEvent.toString());
                    break;
                case "TRADE.SUCCEEDED":
                    TradeSucceededEvent tradeSucceededEvent = objectMapper.convertValue(messageMap, TradeSucceededEvent.class);
                    log.info(tradeSucceededEvent.toString());
                    handleTradeSucceeded(tradeSucceededEvent);
                    log.info(tradeSucceededEvent.toString());
                    break;
                case "TRADE.FAILED":
                    TradeFailedEvent tradeFailedEvent = objectMapper.convertValue(messageMap, TradeFailedEvent.class);
                    log.info(tradeFailedEvent.toString());
                    handleTradeFailed(tradeFailedEvent);
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

    @Transactional
    public void handleTradeRequestAccepted(TradeRequestAcceptedEvent event) {
        TradeRequestAcceptedEvent.TradeRequestAcceptedPayload payload = event.getPayload();
        log.info("TradeRequestAcceptedEvent 처리: tradeId={}", payload.getTradeId());

        Trade trade = tradeRepository.findByTradeId(payload.getTradeId())
                .orElseThrow(() -> new IllegalArgumentException("거래를 찾을 수 없습니다."));

        // 멱등성 체크: 이미 처리된 상태인지 확인
        if ("PENDING".equals(trade.getTradeStatus())) {
            log.info("이미 PENDING 상태인 거래입니다. 중복 이벤트이므로 무시합니다. tradeId={}", payload.getTradeId());
            return;
        }

        trade.setTradeStatus("PENDING");
        tradeRepository.save(trade);
        log.info("거래 상태를 PENDING으로 변경했습니다. tradeId={}", payload.getTradeId());
    }

    @Transactional
    public void handleTradeRequestRejected(TradeRequestRejectedEvent event) {
        TradeRequestRejectedEvent.TradeRequestRejectedPayload payload = event.getPayload();
        log.info("TradeRequestRejectedEvent 처리: tradeId={}", payload.getTradeId());

        Trade trade = tradeRepository.findByTradeId(payload.getTradeId())
                .orElseThrow(() -> new IllegalArgumentException("거래를 찾을 수 없습니다."));

        // 멱등성 체크: 이미 처리된 상태인지 확인 (REJECTED로 변경하는 것을 권장하나, 기존 로직 유지)
        if ("REJECTED".equals(trade.getTradeStatus()) || "PENDING".equals(trade.getTradeStatus())) {
            log.info("이미 REJECTED 또는 PENDING 상태인 거래입니다. 중복 이벤트이므로 무시합니다. tradeId={}", payload.getTradeId());
            return;
        }

        // 참고: 거절 이벤트이므로 'REJECTED' 상태로 변경하는 것이 더 명확해 보입니다.
        trade.setTradeStatus("REJECTED");
        tradeRepository.save(trade);
        log.info("거래 상태를 REJECTED로 변경했습니다. tradeId={}", payload.getTradeId());
    }

    @Transactional
    public void handleTradeSucceeded(TradeSucceededEvent event) {
        TradeSucceededEvent.TradeSucceededPayload payload = event.getPayload();
        log.info("TradeSucceededEvent 처리 시작: tradeId={}", payload.getTradeId());

        Trade trade = tradeRepository.findByTradeId(payload.getTradeId())
                .orElseThrow(() -> new IllegalArgumentException("거래를 찾을 수 없습니다. tradeId: " + payload.getTradeId()));

        // 멱등성 체크: 이미 최종 상태(성공)인지 확인
        if ("SUCCEEDED".equals(trade.getTradeStatus())) {
            log.info("이미 SUCCEEDED 상태인 거래입니다. 중복 이벤트이므로 무시합니다. tradeId={}", payload.getTradeId());
            return;
        }

        // 방어 코드: 다른 최종 상태(실패)일 경우 경고 로그
        if ("FAILED".equals(trade.getTradeStatus())) {
            log.warn("이미 FAILED 상태인 거래에 대해 SUCCEEDED 이벤트가 수신되었습니다. tradeId={}", payload.getTradeId());
        }

        trade.setTradeStatus("SUCCEEDED");
        tradeRepository.save(trade);
        log.info("거래 상태를 SUCCEEDED로 변경했습니다. tradeId={}", payload.getTradeId());
    }

    @Transactional
    public void handleTradeFailed(TradeFailedEvent event) {
        TradeFailedEvent.TradeFailedPayload payload = event.getPayload();
        log.info("TradeFailedEvent 처리: tradeId={}", payload.getTradeId());

        Trade trade = tradeRepository.findByTradeId(payload.getTradeId())
                .orElseThrow(() -> new IllegalArgumentException("거래를 찾을 수 없습니다."));

        // 멱등성 체크: 이미 최종 상태(실패)인지 확인
        if ("FAILED".equals(trade.getTradeStatus())) {
            log.info("이미 FAILED 상태인 거래입니다. 중복 이벤트이므로 무시합니다. tradeId={}", payload.getTradeId());
            return;
        }

        // 방어 코드: 다른 최종 상태(성공)일 경우 경고 로그
        if ("SUCCEEDED".equals(trade.getTradeStatus())) {
            log.warn("이미 SUCCEEDED 상태인 거래에 대해 FAILED 이벤트가 수신되었습니다. tradeId={}", payload.getTradeId());
        }

        trade.setTradeStatus("FAILED");
        tradeRepository.save(trade);
        log.info("거래 상태를 FAILED로 변경했습니다. tradeId={}", payload.getTradeId());
    }
}
