package com.ddiring.backend_market.event.cosumer;

import com.ddiring.backend_market.api.asset.AssetClient;
import com.ddiring.backend_market.api.asset.dto.request.MarketBuyDto;
import com.ddiring.backend_market.api.asset.dto.request.MarketSellDto;
import com.ddiring.backend_market.trade.entity.Orders;
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
    private final TradeService tradeService;
    private final ObjectMapper objectMapper; // ObjectMapper 주입

    @KafkaListener(topics = "BUY_ORDER_INITIATED", groupId = "saga-service-group")
    public void handleBuyOrderInitiated(String message) {
        try {
            // 1. 받은 메시지(String)를 Orders 객체로 변환
            Orders order = objectMapper.readValue(message, Orders.class);

            log.info("Saga: BUY_ORDER_INITIATED 수신. 주문 ID: {}", order.getOrdersId());

            MarketBuyDto marketBuyDto = new MarketBuyDto();
            marketBuyDto.setOrdersId(order.getOrdersId());
            marketBuyDto.setProjectId(order.getProjectId());
            marketBuyDto.setBuyPrice(order.getPurchasePrice());
            marketBuyDto.setTransType(1);

            assetClient.marketBuy(order.getUserSeq(), order.getRole(), marketBuyDto);
            log.info("Saga: Asset 서비스에 구매 요청 성공. 주문 ID: {}", order.getOrdersId());

        } catch (Exception e) {
            // JSON 파싱 실패 또는 Asset 서비스 호출 실패 시
            log.error("Saga: BUY_ORDER_INITIATED 처리 실패. 메시지: {}", message, e);
            // 실패 시 보상 트랜잭션을 호출해야 한다면 여기에 로직을 추가할 수 있습니다.
            // 예를 들어, 메시지에서 orderId만 추출하여 환불을 시도할 수 있습니다.
        }
    }


    @KafkaListener(topics = "SELL_ORDER_INITIATED", groupId = "saga-service-group")
    public void handleSellOrderInitiated(String message) {
        try {
            // 1. 받은 메시지(String)를 Orders 객체로 변환
            Orders order = objectMapper.readValue(message, Orders.class);

            log.info("Saga: SELL_ORDER_INITIATED 수신. 주문 ID: {}", order.getOrdersId());

            MarketSellDto marketSellDto = new MarketSellDto();
            marketSellDto.setOrdersId(order.getOrdersId());
            marketSellDto.setProjectId(order.getProjectId());
            marketSellDto.setSellToken(order.getTokenQuantity());
            marketSellDto.setTransType(2);

            assetClient.marketSell(order.getUserSeq(), marketSellDto);
            log.info("Saga: Asset 서비스에 판매 요청 성공. 주문 ID: {}", order.getOrdersId());

        } catch (Exception e) {
            // JSON 파싱 실패 또는 Asset 서비스 호출 실패 시
            log.error("Saga: SELL_ORDER_INITIATED 처리 실패. 메시지: {}", message, e);
            // 실패 시 보상 트랜잭션을 호출해야 한다면 여기에 로직을 추가할 수 있습니다.
        }
    }
}