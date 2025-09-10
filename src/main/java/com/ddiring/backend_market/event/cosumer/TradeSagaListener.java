package com.ddiring.backend_market.event.cosumer;

import com.ddiring.backend_market.api.asset.AssetClient;
import com.ddiring.backend_market.api.asset.dto.request.MarketBuyDto;
import com.ddiring.backend_market.api.asset.dto.request.MarketSellDto;
import com.ddiring.backend_market.trade.entity.Orders;
import com.ddiring.backend_market.trade.repository.OrdersRepository;
import com.ddiring.backend_market.trade.service.TradeService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class TradeSagaListener {

    private final AssetClient assetClient;
    private final OrdersRepository ordersRepository;
    private final TradeService tradeService;
    private final ObjectMapper objectMapper; // ObjectMapper 주입

    @KafkaListener(topics = "BUY_ORDER_INITIATED", groupId = "saga-service-group")
    public void handleBuyOrderInitiated(String message) {
        Orders buyOrder = null;
        try {

            Orders orderFromMessage = objectMapper.readValue(message, Orders.class);
            log.info("Saga: BUY_ORDER_INITIATED 수신. 주문 ID: {}", orderFromMessage.getOrdersId());

            buyOrder = ordersRepository.findById(orderFromMessage.getOrdersId())
                    .orElseThrow(() -> new IllegalStateException("주문을 찾을 수 없습니다. ID: " + orderFromMessage.getOrdersId()));

            if (!"PENDING".equals(buyOrder.getOrdersStatus())) {
                log.info("이미 처리 시작되었거나 완료된 주문입니다. 중복 이벤트이므로 무시합니다. 주문 ID: {}, 현재 상태: {}",
                        buyOrder.getOrdersId(), buyOrder.getOrdersStatus());
                return;
            }

            MarketBuyDto marketBuyDto = new MarketBuyDto();
            marketBuyDto.setOrdersId(buyOrder.getOrdersId());
            marketBuyDto.setProjectId(buyOrder.getProjectId());
            marketBuyDto.setBuyPrice(buyOrder.getPurchasePrice());
            marketBuyDto.setTransType(1);

            assetClient.marketBuy(buyOrder.getUserSeq(), buyOrder.getRole(), marketBuyDto);
            log.info("Saga: Asset 서비스에 구매 요청(marketBuy) 성공. 주문 ID: {}", buyOrder.getOrdersId());

            buyOrder.setOrdersStatus("SUCCEEDED");
            ordersRepository.save(buyOrder);

            tradeService.beforeMatch(buyOrder);
            log.info("Saga: 구매 주문 최종 처리 성공. 상태를 SUCCEEDED로 변경. 주문 ID: {}", buyOrder.getOrdersId());

        } catch (Exception e) {
            log.error("Saga: BUY_ORDER_INITIATED 처리 실패. 주문 ID: {}. 원인: {}",
                    (buyOrder != null ? buyOrder.getOrdersId() : "알 수 없음"), e.getMessage(), e);

            if (buyOrder != null) {
                buyOrder.setOrdersStatus("FAILED");
                ordersRepository.save(buyOrder);
                log.info("주문 처리 실패. 상태를 FAILED로 변경. 주문 ID: {}", buyOrder.getOrdersId());

                // 환불 로직 호출
                tradeService.buyOrderRefund(buyOrder.getOrdersId());
            }
        }
    }


    @KafkaListener(topics = "SELL_ORDER_INITIATED", groupId = "saga-service-group")
    public void handleSellOrderInitiated(String message) {
        Orders sellOrder = null;
        try {
            // 1. 받은 메시지(String)를 Orders 객체로 변환
            Orders order = objectMapper.readValue(message, Orders.class);

            log.info("Saga: SELL_ORDER_INITIATED 수신. 주문 ID: {}", order.getOrdersId());

            MarketSellDto marketSellDto = new MarketSellDto();
            marketSellDto.setOrdersId(order.getOrdersId());
            marketSellDto.setProjectId(order.getProjectId());
            marketSellDto.setSellToken(order.getTokenQuantity());
            marketSellDto.setTransType(0);

            assetClient.marketSell(order.getUserSeq(), marketSellDto);
            log.info("Saga: Asset 서비스에 판매 요청 성공. 주문 ID: {}", order.getOrdersId());
            sellOrder = ordersRepository.findById(order.getOrdersId()).orElseThrow(() -> new IllegalStateException("주문을 찾을 수 없습니다"));
        } catch (Exception e) {
            // JSON 파싱 실패 또는 Asset 서비스 호출 실패 시
            log.error("Saga: SELL_ORDER_INITIATED 처리 실패. 메시지: {}", message, e);
            tradeService.sellOrderRefund(sellOrder.getOrdersId());
        }
    }
}