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
    private final OrdersRepository ordersRepository;
    private final AssetClient  assetClient;
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

        trade.setTradeStatus("PENDING");
        tradeRepository.save(trade);
    }

    @Transactional
    public void handleTradeRequestRejected(TradeRequestRejectedEvent event) {
        TradeRequestRejectedEvent.TradeRequestRejectedPayload payload = event.getPayload();
        log.info("TradeRequestRejectedEvent 처리: tradeId={}", payload.getTradeId());

        Trade trade = tradeRepository.findByTradeId(payload.getTradeId())
                .orElseThrow(() -> new IllegalArgumentException("거래를 찾을 수 없습니다."));

        trade.setTradeStatus("PENDING");
        tradeRepository.save(trade);
    }

    @Transactional
    public void handleTradeSucceeded(TradeSucceededEvent event) {
        TradeSucceededEvent.TradeSucceededPayload payload = event.getPayload();
        log.info("TradeSucceededEvent 처리 시작: tradeId={}", payload.getTradeId());

        Trade trade = tradeRepository.findByTradeId(payload.getTradeId())
                .orElseThrow(() -> new IllegalArgumentException("거래를 찾을 수 없습니다. tradeId: " + payload.getTradeId()));

        trade.setTradeStatus("SUCCEEDED");
        tradeRepository.save(trade);

        try {

            UpdateAssetRequestDto requestDto = UpdateAssetRequestDto.builder()
                    .tradeId(trade.getTradeId())
                    .projectId(trade.getProjectId())
                    .buyAddress(payload.getBuyerAddress())
                    .buyTokenAmount(payload.getBuyerTokenAmount())
                    .sellAddress(payload.getSellerAddress())
                    .sellPrice(trade.getTradePrice().longValue())
                    .build();

            assetClient.updateAssetsAfterTrade(requestDto);

            log.info("Asset 서비스에 자산 변경 요청 완료. tradeId={}", trade.getTradeId());

        } catch (Exception e) {
            log.error("Asset 서비스 호출 중 심각한 오류 발생. tradeId={}", payload.getTradeId(), e);
            throw new RuntimeException("Asset 서비스 호출 실패", e);
        }
    }

    @Transactional
    public void handleTradeFailed(TradeFailedEvent event) {
        TradeFailedEvent.TradeFailedPayload payload = event.getPayload();
        log.info("TradeFailedEvent 처리: tradeId={}", payload.getTradeId());

        Trade trade = tradeRepository.findByTradeId(payload.getTradeId())
                .orElseThrow(() -> new IllegalArgumentException("거래를 찾을 수 없습니다."));

        trade.setTradeStatus("FAILED");
        tradeRepository.save(trade);


//        try {
//            long amount = (long) trade.getTradePrice();
//            AssetEscrowRequest request = new AssetEscrowRequest(trade.getTradeId(), amount);
//            assetClient.refundEscrowToBuyer(request);
//            log.info("Asset 서비스에 예치금({}) 환불 요청 완료. tradeId={}", amount, trade.getTradeId());
//        } catch (Exception e) {
//            log.error("Asset 서비스 호출(예치금 환불) 실패. tradeId={}", trade.getTradeId(), e);
//        }
    }
}