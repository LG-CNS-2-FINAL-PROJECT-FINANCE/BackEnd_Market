package com.ddiring.backend_market.event.cosumer;


import com.ddiring.backend_market.event.dto.DepositFailedEvent;
import com.ddiring.backend_market.event.dto.DepositSucceededEvent;
import com.ddiring.backend_market.event.dto.TradeRequestAcceptedEvent;
import com.ddiring.backend_market.event.dto.TradeRequestRejectedEvent;
import com.ddiring.backend_market.trade.entity.Orders;
import com.ddiring.backend_market.trade.entity.Trade;
import com.ddiring.backend_market.trade.repository.OrdersRepository;
import com.ddiring.backend_market.trade.repository.TradeRepository;
import com.ddiring.backend_market.trade.service.TradeService;
import com.fasterxml.jackson.core.type.TypeReference;
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
public class KafkaDepositEventsListner {

    private final OrdersRepository ordersRepository;
    private final ObjectMapper objectMapper;
    private final TradeService tradeService;

    @KafkaListener(topics = "DEPOSIT", groupId = "market-service-group")
    public void listenTradeEvents(String message) {
        try {
            Map<String, Object> messageMap = objectMapper.readValue(message, new TypeReference<>() {
            });

            String eventType = (String) messageMap.get("eventType");
            if (eventType == null) {
                log.warn("eventType 필드를 찾을 수 없습니다: {}", message);
                return;
            }

            log.info("수신된 이벤트 타입: {}", eventType);
            switch (eventType) {
                case "DEPOSIT.SUCCEEDED":
                    DepositSucceededEvent depositSucceededEvent = objectMapper.convertValue(messageMap, DepositSucceededEvent.class);
                    log.info(depositSucceededEvent.toString());
                    handleDepositSucceeded(depositSucceededEvent);
                    log.info(depositSucceededEvent.toString());
                    break;
                case "DEPOSIT.FAILED":
                    DepositFailedEvent depositFailedEvent = objectMapper.convertValue(messageMap, DepositFailedEvent.class);
                    log.info(depositFailedEvent.toString());
                    handleDepositFailed(depositFailedEvent);
                    log.info(depositFailedEvent.toString());
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
    public void handleDepositSucceeded(DepositSucceededEvent event) {
        DepositSucceededEvent.DepositSucceededPayload payload = event.getPayload();
        if (payload == null || payload.getSellId() == null) {
            log.error("DepositSucceededEvent의 payload 또는 sellId가 null입니다.");
            return;
        }

        log.info("입금 성공 처리 시작: orderId={}", payload.getSellId());

        Orders order = ordersRepository.findById(Math.toIntExact(payload.getSellId()))
                .orElse(null);

        if (order == null) {
            log.warn("입금 성공 이벤트를 처리할 주문을 찾지 못했습니다: orderId={}", payload.getSellId());
            return;
        }

        order.setOrdersStatus("SUCCEEDED");
        Orders updatedOrder = ordersRepository.save(order);
        log.info("주문 ID {}의 상태를 SUCCEEDED로 변경했습니다.", updatedOrder.getOrdersId());

        tradeService.beforeMatch(updatedOrder);
    }

    @Transactional
    public void handleDepositFailed(DepositFailedEvent event) {
        DepositFailedEvent.DepositFailedPayload payload = event.getPayload();
        log.info("DepositSucceededEvent 처리: sellId={}", payload.getSellId());

        Orders orders = ordersRepository.findByOrdersId(Math.toIntExact(payload.getSellId()))
                .orElseThrow(() -> new IllegalArgumentException("거래를 찾을 수 없습니다."));

        orders.setOrdersStatus(payload.getStatus());
        ordersRepository.save(orders);
    }

}
