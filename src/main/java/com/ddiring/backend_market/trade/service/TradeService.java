package com.ddiring.backend_market.trade.service;

import com.ddiring.backend_market.api.asset.AssetClient;
import com.ddiring.backend_market.api.asset.dto.request.*;
import com.ddiring.backend_market.api.asset.dto.request.MarketRefundDto;
import com.ddiring.backend_market.api.blockchain.BlockchainClient;
import com.ddiring.backend_market.api.blockchain.dto.trade.BuyInfoDto;
import com.ddiring.backend_market.api.blockchain.dto.trade.SellInfoDto;
import com.ddiring.backend_market.api.blockchain.dto.trade.TradeDto;
import com.ddiring.backend_market.api.user.SignDto;
import com.ddiring.backend_market.api.user.UserClient;
import com.ddiring.backend_market.common.dto.ApiResponseDto;
import com.ddiring.backend_market.common.exception.BadParameter;
import com.ddiring.backend_market.common.exception.NotFound;
import com.ddiring.backend_market.event.dto.TradePriceUpdateEvent;
import com.ddiring.backend_market.event.producer.TradeEventProducer;
import com.ddiring.backend_market.trade.dto.*;
import com.ddiring.backend_market.trade.entity.History;
import com.ddiring.backend_market.trade.entity.Orders;
import com.ddiring.backend_market.trade.entity.Trade;
import com.ddiring.backend_market.trade.repository.HistoryRepository;
import com.ddiring.backend_market.trade.repository.OrdersRepository;
import com.ddiring.backend_market.trade.repository.TradeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TradeService {
    private final OrdersRepository ordersRepository;
    private final TradeRepository tradeRepository;
    private final HistoryRepository historyRepository;
    private final AssetClient assetClient;
    private final UserClient userClient;
    private final TradeEventProducer tradeEventProducer;
    private final BlockchainClient blockchainClient;

    private void matchAndExecuteTrade(Orders order, List<Orders> oldOrders) {
        for (Orders oldOrder : oldOrders) {
            boolean tradePossible = false;
            if (order.getOrdersType() == 1 && order.getPerPrice() >= oldOrder.getPerPrice()) {
                tradePossible = true;
            } else if (order.getOrdersType() == 0 && order.getPerPrice() <= oldOrder.getPerPrice()) {
                tradePossible = true;
            }

            if (tradePossible) {
                int tradedQuantity = Math.min(order.getTokenQuantity(), oldOrder.getTokenQuantity());
                int tradePrice = order.getOrdersType() == 1 ? oldOrder.getPerPrice() : order.getPerPrice();
                log.info("거래 체결 시작. 구매 주문 ID: {}, 판매 주문 ID: {}, 체결 가격: {}, 총 가격: {}, 토큰 갯수: {}", (order.getOrdersType() == 1 ? order.getUserSeq() : oldOrder.getUserSeq()), (order.getOrdersType() == 0 ? order.getUserSeq() : oldOrder.getUserSeq()), tradePrice, tradePrice * tradedQuantity, tradedQuantity);
                Orders purchaseOrderForLog = order.getOrdersType() == 1 ? order : oldOrder;
                Orders sellOrderForLog = order.getOrdersType() == 0 ? order : oldOrder;
                log.info("구매 주문 정보: userSeq={}, ordersId={}", purchaseOrderForLog.getUserSeq(),
                        purchaseOrderForLog.getOrdersId());
                log.info("판매 주문 정보: userSeq={}, ordersId={}", sellOrderForLog.getUserSeq(),
                        sellOrderForLog.getOrdersId());
                Trade trade = Trade.builder()
                        .projectId(order.getProjectId())
                        .purchaseId(order.getOrdersType() == 1 ? order.getUserSeq() : oldOrder.getUserSeq())
                        .sellId(order.getOrdersType() == 0 ? order.getUserSeq() : oldOrder.getUserSeq())
                        .buyerAddress(order.getOrdersType() == 1 ? order.getWalletAddress() : oldOrder.getWalletAddress())
                        .sellerAddress(order.getOrdersType() == 0 ? order.getWalletAddress() : oldOrder.getWalletAddress())
                        .tradePrice(tradePrice * tradedQuantity)
                        .tokenPerPrice(tradePrice)
                        .tokenQuantity(tradedQuantity)
                        .tradedAt(LocalDateTime.now())
                        .tradeStatus("PENDING")
                        .build();

                tradeRepository.save(trade);

                BuyInfoDto buyInfo = BuyInfoDto.builder()
                        .buyId(Long.valueOf(order.getOrdersType() == 1 ? order.getOrdersId() : oldOrder.getOrdersId()))
                        .buyerAddress(order.getOrdersType() == 1 ? order.getWalletAddress() : oldOrder.getWalletAddress())
                        .build();

                SellInfoDto sellInfo = SellInfoDto.builder()
                        .sellId(Long.valueOf(order.getOrdersType() == 0 ? order.getOrdersId() : oldOrder.getOrdersId()))
                        .sellerAddress(order.getOrdersType() == 0 ? order.getWalletAddress() : oldOrder.getWalletAddress())
                        .build();

                TradeDto tradeDto = TradeDto.builder()
                        .tradeId(trade.getTradeId())
                        .projectId(order.getProjectId())
                        .buyInfo(buyInfo)
                        .sellInfo(sellInfo)
                        .tokenAmount((long) tradedQuantity)
                        .pricePerToken((long) (tradePrice / tradedQuantity))
                        .build();


                // blockchainClient.requestTradeTokenMove(tradeDto);

                TradePriceUpdateEvent priceUpdateEvent = TradePriceUpdateEvent.of(order.getProjectId(), tradePrice);
                tradeEventProducer.send(TradePriceUpdateEvent.TOPIC, priceUpdateEvent);

                TitleRequestDto titleRequestDto = new TitleRequestDto();
                titleRequestDto.setProjectId(order.getProjectId());
                String title = assetClient.getMarketTitle(titleRequestDto);

                log.info("거래 체결 완료. 구매 주문 ID: {}, 판매 주문 ID: {}, 체결 가격: {}, 총 가격: {}, 토큰 갯수: {}", (order.getOrdersType() == 1 ? order.getUserSeq() : oldOrder.getUserSeq()), (order.getOrdersType() == 0 ? order.getUserSeq() : oldOrder.getUserSeq()), tradePrice, tradePrice * tradedQuantity, tradedQuantity);
                History purchaseHistory = History.builder()
                        .title(title)
                        .projectId(order.getProjectId())
                        .userSeq(order.getOrdersType() == 1 ? order.getUserSeq() : oldOrder.getUserSeq())
                        .tradeType(1)
                        .tradePrice(tradePrice * tradedQuantity)
                        .perPrice(tradePrice)
                        .tokenQuantity(tradedQuantity)
                        .tradedAt(LocalDateTime.now())
                        .build();
                historyRepository.save(purchaseHistory);

                History sellHistory = History.builder()
                        .title(title)
                        .projectId(order.getProjectId())
                        .userSeq(order.getOrdersType() == 0 ? order.getUserSeq() : oldOrder.getUserSeq())
                        .tradeType(0)
                        .tradePrice(tradePrice * tradedQuantity)
                        .perPrice(tradePrice)
                        .tokenQuantity(tradedQuantity)
                        .tradedAt(LocalDateTime.now())
                        .build();
                historyRepository.save(sellHistory);

                order.updateOrder(null, order.getTokenQuantity() - tradedQuantity);
                oldOrder.updateOrder(null, oldOrder.getTokenQuantity() - tradedQuantity);

                if (order.getTokenQuantity() == 0) {
                    ordersRepository.delete(order);
                } else {
                    ordersRepository.save(order);
                }

                if (oldOrder.getTokenQuantity() == 0) {
                    ordersRepository.delete(oldOrder);
                } else {
                    ordersRepository.save(oldOrder);
                }

                if (order.getTokenQuantity() == 0) {
                    break;
                }
            }
            else {
                log.info("바보임?");
//                log.info("거래 체결 실패. 구매 주문 ID: {}, 판매 주문 ID: {}", (order.getOrdersType() == 1 ? order.getUserSeq() : oldOrder.getUserSeq()), (order.getOrdersType() == 0 ? order.getUserSeq() : oldOrder.getUserSeq()));

            }
        }
    }

    @Transactional
    public void sellReception(String userSeq, String role, OrdersRequestDto ordersRequestDto) {
        if (userSeq == null || ordersRequestDto.getProjectId() == null || ordersRequestDto.getOrdersType() == null
                || ordersRequestDto.getTokenQuantity() <= 0 || role == null) {
            throw new BadParameter("필수 파라미터가 누락되었습니다.");
        }
        if (ordersRequestDto.getOrdersType() == 1) {
            throw new BadParameter("이거 아이다 다른거 줘라");
        }

        ApiResponseDto<String> response = assetClient.getWalletAddress(userSeq);
        String walletAddress = response.getData();
        log.info("판매 주문 접수: Asset 서비스에서 지갑 주소 조회 완료. walletAddress={}", walletAddress);

        Orders order = Orders.builder()
                .userSeq(userSeq)
                .projectId(ordersRequestDto.getProjectId())
                .role(role)
                .ordersType(ordersRequestDto.getOrdersType())
                .purchasePrice(ordersRequestDto.getPurchasePrice() * ordersRequestDto.getTokenQuantity())
                .perPrice(ordersRequestDto.getPurchasePrice())
                .tokenQuantity(ordersRequestDto.getTokenQuantity())
                .walletAddress(walletAddress)
                .registedAt(LocalDateTime.now())
                .build();

        Orders savedOrder = ordersRepository.save(order);

        OrderDeleteDto orderDeleteDto = new OrderDeleteDto();
        orderDeleteDto.setOrderId(savedOrder.getOrdersId());

        MarketSellDto marketSellDto = new MarketSellDto();
        marketSellDto.setOrdersId(savedOrder.getOrdersId());
        marketSellDto.setProjectId(ordersRequestDto.getProjectId());
        marketSellDto.setSellToken(ordersRequestDto.getTokenQuantity());
        marketSellDto.setTransType(2);

        SignDto signDto = new SignDto();
        signDto.setProjectId(ordersRequestDto.getProjectId());
        signDto.setUserAddress(walletAddress);
        signDto.setTokenQuantity(ordersRequestDto.getTokenQuantity());

        log.info("판매 주문 접수: Asset 서비스에서 지갑 주소 조회 완료. walletAddress={}", walletAddress);
        try {

            assetClient.marketSell(userSeq, marketSellDto);
            userClient.sign(signDto);
            ordersRepository.save(order);

            logSales(userSeq, false, savedOrder.getOrdersId(), ordersRequestDto);
//            log.info("판매 주문 접수: 판매 주문 ID: {},프로젝트 ID {}, 체결 가격: {}, 총 가격: {}, 토큰 갯수: {}", userSeq, ordersRequestDto.getProjectId(), ordersRequestDto.getPurchasePrice(), ordersRequestDto.getPurchasePrice() * ordersRequestDto.getTokenQuantity(), ordersRequestDto.getTokenQuantity());

        } catch (Exception e) {
            throw new RuntimeException("Asset 서비스 통신 중 오류가 발생했습니다.", e);
        }
        List<Orders> purchaseOrder = ordersRepository
                .findByProjectIdAndOrdersTypeOrderByPurchasePriceDescRegistedAtAsc(ordersRequestDto.getProjectId(), 1);
        matchAndExecuteTrade(savedOrder, purchaseOrder);

    }

    @Transactional
    public void buyReception(String userSeq, String role, OrdersRequestDto ordersRequestDto) {
        if (userSeq == null || ordersRequestDto.getProjectId() == null || ordersRequestDto.getOrdersType() == null
                || ordersRequestDto.getTokenQuantity() <= 0 || role == null) {
            throw new BadParameter("필수 파라미터가 누락되었습니다.");
        }
        if (ordersRequestDto.getOrdersType() == 0) {
            throw new BadParameter("이거 아이다 다른거 줘라");
        }
        ApiResponseDto<String> response = assetClient.getWalletAddress(userSeq);
        String walletAddress = response.getData();
        log.info("구매접수: Asset 서비스에서 지갑 주소 조회 완료. walletAddress={}", walletAddress);
        Orders order = Orders.builder()
                .userSeq(userSeq)
                .projectId(ordersRequestDto.getProjectId())
                .role(role)
                .walletAddress(walletAddress)
                .ordersType(ordersRequestDto.getOrdersType())
                .purchasePrice(ordersRequestDto.getPurchasePrice() * ordersRequestDto.getTokenQuantity())
                .tokenQuantity(ordersRequestDto.getTokenQuantity())
                .registedAt(LocalDateTime.now())
                .build();

        Orders savedOrder = ordersRepository.save(order);

        OrderDeleteDto orderDeleteDto = new OrderDeleteDto();
        orderDeleteDto.setOrderId(savedOrder.getOrdersId());

        MarketBuyDto marketBuyDto = new MarketBuyDto();
        marketBuyDto.setOrdersId(savedOrder.getOrdersId());
        marketBuyDto.setProjectId(ordersRequestDto.getProjectId());
        marketBuyDto.setBuyPrice((int) (ordersRequestDto.getPurchasePrice() * ordersRequestDto.getTokenQuantity() + (ordersRequestDto.getTokenQuantity() * ordersRequestDto.getTokenQuantity() * 0.03)));
        marketBuyDto.setTransType(1);

        try {
//            log.info("구매 주문 접수: 구매 주문 ID: {},프로젝트 ID {}, 체결 가격: {}, 총 가격: {}, 토큰 갯수: {}", userSeq, ordersRequestDto.getProjectId(), ordersRequestDto.getPurchasePrice(), ordersRequestDto.getPurchasePrice() * ordersRequestDto.getTokenQuantity(), ordersRequestDto.getTokenQuantity());
            assetClient.marketBuy(userSeq, role, marketBuyDto);
            logSales(userSeq, true, savedOrder.getOrdersId(), ordersRequestDto);
//            log.info("구매 주문 접수: Asset 서비스에 예치금 요청 완료. userSeq={}", userSeq);
        } catch (Exception e) {
            log.error("Asset 서비스 입금 요청 실패: {}", e.getMessage());
            throw new RuntimeException("Asset 서비스 통신 중 오류가 발생했습니다.", e);
        }

        List<Orders> sellOrder = ordersRepository
                .findByProjectIdAndOrdersTypeOrderByPurchasePriceAscRegistedAtAsc(ordersRequestDto.getProjectId(), 0);
        matchAndExecuteTrade(savedOrder, sellOrder);

    }

    @Transactional
    public void deleteOrder(String userSeq, String role, OrderDeleteDto orderDeleteDto) {
        if (orderDeleteDto.getOrderId() == null || userSeq == null || role == null) {
            throw new BadParameter("필요한 거 누락되었습니다.");
        }

        Orders order = ordersRepository.findByOrdersId(orderDeleteDto.getOrderId())
                .orElseThrow(() -> new NotFound("권한 가져와"));

            MarketRefundDto marketRefundDto = new MarketRefundDto();
            marketRefundDto.setOrdersId(orderDeleteDto.getOrderId());
            marketRefundDto.setProjectId(order.getProjectId());
            marketRefundDto.setRefundPrice(order.getPurchasePrice());
            marketRefundDto.setRefundAmount(order.getTokenQuantity());
            marketRefundDto.setOrderType(order.getOrdersType());
            assetClient.marketRefund(userSeq, role, marketRefundDto);

            ordersRepository.delete(order);
        log.info("주문 삭제: 삭제 주문 ID: {}, 프로젝트 ID {}, 주문 번호: {}", orderDeleteDto.getOrderId(), order.getProjectId(), orderDeleteDto.getOrderId());

    }

    @Transactional(readOnly = true)
    public List<OrderHistoryResponseDto> getTradeHistory(String projectId) {
        if (projectId == null) {
            throw new BadParameter("번호 내놔");
        }
        List<Trade> trades = tradeRepository.findTop20ByProjectIdOrderByTradedAtDesc(projectId);
        if (trades.isEmpty()) {
            return List.of();
        }
        return trades.stream()
                .map(OrderHistoryResponseDto::new)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<OrdersResponseDto> getPurchaseOrders(String projectId) {
        if (projectId == null) {
            throw new BadParameter("프로젝트 번호가 필요합니다.");
        }
        // 구매자의 매수 주문서 조회
        List<Orders> orders = ordersRepository
                .findByProjectIdAndOrdersTypeOrderByPurchasePriceDescRegistedAtAsc(projectId, 1);
        return orders.stream()
                .map(OrdersResponseDto::new)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<OrdersResponseDto> getSellOrders(String projectId) {
        if (projectId == null) {
            throw new BadParameter("프로젝트 번호가 필요합니다.");
        }
        // 판매자의 매도 주문서 조회
        List<Orders> orders = ordersRepository
                .findByProjectIdAndOrdersTypeOrderByPurchasePriceAscRegistedAtAsc(projectId, 0);
        return orders.stream()
                .map(OrdersResponseDto::new)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<OrderUserHistory> getUserOrder(String userSeq, String projectId) {
        if (userSeq == null || projectId == null) {
            throw new BadParameter("이제 그만");
        }
        List<Orders> ordersList = ordersRepository.findByUserSeqAndProjectIdOrderByRegistedAtDesc(userSeq, projectId);

        return ordersList.stream()
                .map(OrderUserHistory::new)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<TradeHistoryResponseDto> getTradeHistory(String userSeq, String projectId) {
        if (userSeq == null || projectId == null) {
            throw new BadParameter("이제 그만");
        }
        List<History> tradeHistory = historyRepository.findByUserSeqAndProjectIdOrderByTradedAtDesc(userSeq, projectId);

        return tradeHistory.stream()
                .map(TradeHistoryResponseDto::new)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<TradeHistoryResponseDto> getTradeAllHistory(String userSeq) {
        if (userSeq == null) {
            throw new BadParameter("이제 그만");
        }
        List<History> tradeAllHistory = historyRepository.findByUserSeqOrderByTradedAtDesc(userSeq);

        return tradeAllHistory.stream()
                .map(TradeHistoryResponseDto::new)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<TradeHistoryResponseDto> getAdminHistory() {
        List<History> tradeAllHistory = historyRepository.findAllByOrderByTradedAtDesc();

        return tradeAllHistory.stream()
                .map(TradeHistoryResponseDto::new)
                .collect(Collectors.toList());
    }

    public VerifyTradeDto.Response verifyTrade(VerifyTradeDto.Request requestDto) {
        log.info("[Trade] 검증 데이터 - TradeID: {}, BuyID: {}, SellID: {}, TradeAmount: {}", requestDto.getTradeId(), requestDto.getBuyId(), requestDto.getSellId(), requestDto.getTradeAmount());

        Boolean isExisted = tradeRepository.existsByTradeIdAndPurchaseIdAndSellIdAndTokenQuantity(
                requestDto.getTradeId(),
                requestDto.getBuyId().toString(),
                requestDto.getSellId().toString(),
                requestDto.getTradeAmount()
        );

        log.info("[Trade] 검증 결과 : {}", isExisted);

        return VerifyTradeDto.Response.builder().result(isExisted).build();
    }

    @Transactional(readOnly = true)
    public TradeInfoResponseDto getTradeInfoById(Long tradeId) {
        Trade trade = tradeRepository.findByTradeId(tradeId)
                .orElseThrow(() -> new IllegalArgumentException("거래 정보를 찾을 수 없습니다: " + tradeId));

        return TradeInfoResponseDto.builder()
                .tradeId(trade.getTradeId())
                .projectId(trade.getProjectId())
                .price(trade.getTradePrice()) // 총 거래 금액
                .tokenQuantity(trade.getTokenQuantity()) // 거래된 토큰 수량
                .buyerUserSeq(trade.getPurchaseId())   // 구매자 userSeq
                .sellerUserSeq(trade.getSellId()) // 판매자 userSeq
                .build();
    }

    public void logSales(String userSeq, Boolean isPurchaseFlag, Integer orderId, OrdersRequestDto requestDto) {
        try {
            MDC.put("userSeq", userSeq);
            MDC.put("orderId", orderId.toString());
            MDC.put("projectId", requestDto.getProjectId());
            MDC.put("purchasePrice", requestDto.getPurchasePrice().toString());
            MDC.put("tokenQuantity", requestDto.getTokenQuantity().toString());
            MDC.put("ordersType", requestDto.getOrdersType().toString());

            if(isPurchaseFlag){
                log.info("purchase log captured.");
            }
            else{
                log.info("sales log captured.");
            }
        }
        finally {
            MDC.clear();
        }
    }
}