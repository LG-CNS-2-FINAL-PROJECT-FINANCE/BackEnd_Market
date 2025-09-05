package com.ddiring.backend_market.event.cosumer;

import com.ddiring.backend_market.api.asset.AssetClient;
import com.ddiring.backend_market.api.asset.dto.request.MarketBuyDto;
import com.ddiring.backend_market.api.asset.dto.request.MarketSellDto;
import com.ddiring.backend_market.event.producer.TradeEventProducer;
import com.ddiring.backend_market.trade.entity.Orders;
import com.ddiring.backend_market.trade.repository.OrdersRepository;
import com.ddiring.backend_market.trade.service.TradeService;
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
    private final TradeEventProducer tradeEventProducer;
    private final OrdersRepository ordersRepository;


    @KafkaListener(topics = "BUY_ORDER_INITIATED")
    public void handleBuyOrderInitiated(Orders order) {
        log.info("Saga: BUY_ORDER_INITIATED 수신. 주문 ID: {}", order.getOrdersId());
        try {
            MarketBuyDto marketBuyDto = new MarketBuyDto();
            marketBuyDto.setOrdersId(order.getOrdersId());
            marketBuyDto.setProjectId(order.getProjectId());
            marketBuyDto.setBuyPrice(order.getPurchasePrice());
            marketBuyDto.setTransType(1);
            assetClient.marketBuy(order.getUserSeq(), order.getRole(), marketBuyDto);
            log.info("Saga: Asset 서비스에 구매 요청 성공. 주문 ID: {}", order.getOrdersId());
        } catch (Exception e) {
            log.error("Saga: Asset 서비스 구매 요청 실패. 주문 ID: {}", order.getOrdersId(), e);
            tradeService.buyOrderRefund(order.getOrdersId());
        }
    }


    @KafkaListener(topics = "SELL_ORDER_INITIATED")
    public void handleSellOrderInitiated(Orders order) {
        log.info("Saga: SELL_ORDER_INITIATED 수신. 주문 ID: {}", order.getOrdersId());
        try {
            MarketSellDto marketSellDto = new MarketSellDto();
            marketSellDto.setOrdersId(order.getOrdersId());
            marketSellDto.setProjectId(order.getProjectId());
            marketSellDto.setSellToken(order.getTokenQuantity());
            marketSellDto.setTransType(2);
            assetClient.marketSell(order.getUserSeq(), marketSellDto);
            log.info("Saga: Asset 서비스에 판매 요청 성공. 주문 ID: {}", order.getOrdersId());

        } catch (Exception e) {
            log.error("Saga: Asset 서비스 판매 요청 실패. 주문 ID: {}", order.getOrdersId(), e);
            tradeService.sellOrderRefund(order.getOrdersId());
        }
    }
}