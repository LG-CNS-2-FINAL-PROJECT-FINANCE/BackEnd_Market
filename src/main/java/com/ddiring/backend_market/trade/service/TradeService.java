package com.ddiring.backend_market.trade.service;

import com.ddiring.backend_market.api.asset.AssetClient;
import com.ddiring.backend_market.api.asset.dto.request.AssetEscrowRequest;
import com.ddiring.backend_market.api.asset.dto.request.LockFundsRequestDto;
import com.ddiring.backend_market.api.asset.dto.request.UnlockFundsRequestDto;
import com.ddiring.backend_market.api.escrow.EscrowClient;
import com.ddiring.backend_market.api.escrow.dto.SettleTradeRequestDto;
import com.ddiring.backend_market.common.exception.BadParameter;
import com.ddiring.backend_market.common.exception.NotFound;
import com.ddiring.backend_market.event.dto.*;
import com.ddiring.backend_market.trade.dto.*;
import com.ddiring.backend_market.trade.entity.History;
import com.ddiring.backend_market.trade.entity.Orders;
import com.ddiring.backend_market.trade.entity.Trade;
import com.ddiring.backend_market.trade.repository.HistoryRepository;
import com.ddiring.backend_market.trade.repository.OrdersRepository;
import com.ddiring.backend_market.trade.repository.TradeRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

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
    private final EscrowClient escrowClient;

    @Transactional
    public void receivePurchaseOrder(String userSeq, OrdersRequestDto dto) {
        Orders order = saveNewOrder(userSeq, dto, 1, "PENDING");
        try {
            int totalAmount = dto.getPurchasePrice() * dto.getTokenQuantity();
            LockFundsRequestDto lockRequest = new LockFundsRequestDto(userSeq, String.valueOf(order.getOrdersId()), totalAmount);
            assetClient.lockFunds(lockRequest);
            order.setOrdersStatus("ACTIVE");
            ordersRepository.save(order);
            log.info("구매 주문 활성화 완료. orderId: {}", order.getOrdersId());
            matchAndExecuteTrade(order);
        } catch (Exception e) {
            handleOrderFailure(order, "자금 동결 실패", e);
        }
    }

    @Transactional
    public void receiveSellOrder(String userSeq, OrdersRequestDto dto) {
        Orders order = saveNewOrder(userSeq, dto, 0, "ACTIVE");
        log.info("판매 주문 접수 및 활성화 완료. orderId: {}", order.getOrdersId());
        matchAndExecuteTrade(order);
    }

    @Transactional
    public void cancelOrder(String userSeq, Integer orderId) {
        Orders order = ordersRepository.findByOrdersIdAndUserSeq(orderId, userSeq)
                .orElseThrow(() -> new NotFound("주문을 찾을 수 없거나 권한이 없습니다."));
        if (order.getOrdersType() == 1) {
            UnlockFundsRequestDto unlockRequest = new UnlockFundsRequestDto(String.valueOf(orderId), userSeq);
            assetClient.unlockFunds(unlockRequest);
        } else {
            // Kafka로 토큰 동결 해제 명령 발행 로직
        }
        ordersRepository.delete(order);
        log.info("주문 취소 완료. orderId: {}", orderId);
    }

    // =================================================================
    // == 2. 거래 매칭 및 정산 (내부 로직) ==
    // =================================================================

    private void matchAndExecuteTrade(Orders newOrder) {
        if (!"ACTIVE".equals(newOrder.getOrdersStatus())) return;

        int counterOrderType = newOrder.getOrdersType() == 1 ? 0 : 1;
        List<Orders> counterOrders = (counterOrderType == 0)
                ? ordersRepository.findByProjectIdAndOrdersTypeAndOrdersStatusOrderByPurchasePriceAscRegistedAtAsc(newOrder.getProjectId(), 0, "ACTIVE")
                : ordersRepository.findByProjectIdAndOrdersTypeAndOrdersStatusOrderByPurchasePriceDescRegistedAtAsc(newOrder.getProjectId(), 1, "ACTIVE");

        for (Orders oldOrder : counterOrders) {
            boolean tradePossible = (newOrder.getOrdersType() == 1 && newOrder.getPurchasePrice() >= oldOrder.getPurchasePrice()) ||
                    (newOrder.getOrdersType() == 0 && newOrder.getPurchasePrice() <= oldOrder.getPurchasePrice());
            if (tradePossible) {
                int tradedQuantity = Math.min(newOrder.getTokenQuantity(), oldOrder.getTokenQuantity());
                int tradePrice = newOrder.getOrdersType() == 1 ? oldOrder.getPurchasePrice() : newOrder.getPurchasePrice();
                Orders purchaseOrder = newOrder.getOrdersType() == 1 ? newOrder : oldOrder;
                Orders sellOrder = newOrder.getOrdersType() == 0 ? newOrder : oldOrder;

                Trade trade = createPendingTrade(purchaseOrder, sellOrder, tradePrice, tradedQuantity);
                try {
                    SettleTradeRequestDto settleRequest = new SettleTradeRequestDto(String.valueOf(trade.getTradeId()), purchaseOrder.getUserSeq(), sellOrder.getUserSeq(), tradePrice * tradedQuantity, tradedQuantity);
                    escrowClient.settleTrade(settleRequest);
                    log.info("Escrow 서비스에 정산 요청 완료. tradeId: {}", trade.getTradeId());
                } catch (Exception e) {
                    log.error("Escrow 서비스 정산 요청 실패. tradeId={}", trade.getTradeId(), e);
                    trade.setTradeStatus("ERROR");
                    tradeRepository.save(trade);
                    // TODO: 복구 로직
                }

                newOrder.setTokenQuantity(newOrder.getTokenQuantity() - tradedQuantity);
                oldOrder.setTokenQuantity(oldOrder.getTokenQuantity() - tradedQuantity);
                if (newOrder.getTokenQuantity() == 0) ordersRepository.delete(newOrder); else ordersRepository.save(newOrder);
                if (oldOrder.getTokenQuantity() == 0) ordersRepository.delete(oldOrder); else ordersRepository.save(oldOrder);

                if (newOrder.getTokenQuantity() == 0) break;
            }
        }
    }
    // =================================================================
    // == Helper Methods (새로 추가되거나 수정된 지원 메소드) ==
    // =================================================================

    private Orders saveNewOrder(String userSeq, OrdersRequestDto dto, int orderType, String status) {
        return ordersRepository.save(Orders.builder()
                .userSeq(userSeq)
                .projectId(dto.getProjectId())
                .ordersType(orderType)
                .purchasePrice(dto.getPurchasePrice())
                .tokenQuantity(dto.getTokenQuantity())
                .ordersStatus(status)
                .registedAt(LocalDateTime.now())
                .build());
    }

    private void handleOrderFailure(Orders order, String message, Exception e) {
        log.error("{} orderId={}", message, order.getOrdersId(), e);
        order.setOrdersStatus("FAILED");
        ordersRepository.save(order);
        throw new RuntimeException("주문 처리 중 오류 발생", e);
    }

    private Trade createPendingTrade(Orders purchaseOrder, Orders sellOrder, int price, int quantity) {
        return tradeRepository.save(Trade.builder()
                .purchaseId(purchaseOrder.getOrdersId())
                .sellId(sellOrder.getOrdersId())
                .projectId(purchaseOrder.getProjectId())
                .tradePrice(price)
                .tokenQuantity(quantity)
                .tradeStatus("PENDING")
                .tradedAt(LocalDateTime.now())
                .build());
    }

    @Transactional
    public Orders updateOrder(Integer ordersId, String userSeq, String projectId, Integer purchasePrice, Integer tokenQuantity, Integer ordersType) {
        if (ordersId == null || userSeq == null || projectId == null || (purchasePrice == null && tokenQuantity == null)) {
            throw new BadParameter("필요한 거 누락되었습니다.");
        }

        Orders order = ordersRepository.findByOrdersIdAndUserSeqAndProjectId(ordersId, userSeq, projectId)
                .orElseThrow(() -> new NotFound("권한 가져와")); // NotFound.java

        if (!order.getOrdersType().equals(ordersType)) {
            throw new BadParameter("같은 오더 잖아 혼난다");
        }

        // 구매자의 구매 입찰 수정, 판매자의 판매 입찰 수정
        order.updateOrder(purchasePrice, tokenQuantity);
        return ordersRepository.save(order);
    }

    @Transactional
    public List<OrderHistoryResponseDto> getTradeHistory(String projectId) {
        if (projectId == null) {
            throw new BadParameter("번호 내놔");
        }
        List<Trade> trades = tradeRepository.findTop20ByProjectIdOrderByTradedAtDesc(projectId);
        return trades.stream()
                .map(OrderHistoryResponseDto::new)
                .collect(Collectors.toList());
    }

    @Transactional
    public List<OrdersResponseDto> getPurchaseOrders(String projectId) {
        if (projectId == null) {
            throw new BadParameter("프로젝트 번호가 필요합니다.");
        }
        // 구매자의 매수 주문서 조회
        List<Orders> orders = ordersRepository.findByProjectIdAndOrdersTypeOrderByPurchasePriceDescRegistedAtAsc(projectId, 1);
        return orders.stream()
                .map(OrdersResponseDto::new)
                .collect(Collectors.toList());
    }

    @Transactional
    public List<OrdersResponseDto> getSellOrders(String projectId) {
        if (projectId == null) {
            throw new BadParameter("프로젝트 번호가 필요합니다.");
        }
        // 판매자의 매도 주문서 조회
        List<Orders> orders = ordersRepository.findByProjectIdAndOrdersTypeOrderByPurchasePriceAscRegistedAtAsc(projectId, 0);
        return orders.stream()
                .map(OrdersResponseDto::new)
                .collect(Collectors.toList());
    }

    @Transactional
    public List<TradeSearchResponseDto> getUserInfo(String userSeq) {
        if (userSeq == null) {
            throw new BadParameter("넌 누구냐");
        }
        List<Orders> ordersHistory = ordersRepository.findByUserSeq(userSeq);

        return ordersHistory.stream()
                .flatMap(orders -> {
                    List<Trade> trades = tradeRepository.findByPurchaseIdOrSellId(orders.getOrdersId(), orders.getOrdersId());

                    return trades.stream()
                            .map(trade -> {
                                Integer orderType = trade.getPurchaseId().equals(orders.getOrdersId()) ? 1 : 0;
                                return new TradeSearchResponseDto(trade, orderType);
                            });
                })
                .sorted((a, b) -> b.getTradedAt().compareTo(a.getTradedAt()))
                .collect(Collectors.toList());
    }
    @Transactional
    public List<TradeHistoryResponseDto> getTradeHistory(String userSeq, Integer tradeType) {
        if (userSeq == null || tradeType == null) {
            throw new BadParameter("이제 그만");
        }
        List<History> tradeHistory = historyRepository.findByUserSeqAndTradeTypeOrderByTradedAtDesc(userSeq, tradeType);

        return tradeHistory.stream()
                .map(TradeHistoryResponseDto::new)
                .collect(Collectors.toList());
    }

    @Transactional
    public List<TradeHistoryResponseDto> getTradeAllHistory(String userSeq) {
        if (userSeq == null) {
            throw new BadParameter("이제 그만");
        }
        List<History> tradeAllHistory = historyRepository.findByUserSeqOrderByTradedAtDesc(userSeq);

        return tradeAllHistory.stream()
                .map(TradeHistoryResponseDto::new)
                .collect(Collectors.toList());
    }

    @Transactional
    public List<TradeHistoryResponseDto> getAdminHistory() {
        List<History> tradeAllHistory = historyRepository.findAllByOrderByTradedAtDesc();

        return tradeAllHistory.stream()
                .map(TradeHistoryResponseDto::new)
                .collect(Collectors.toList());
    }

    @Transactional
    public void handleDepositSucceeded(DepositSucceededEvent event) {
        DepositSucceededEvent.DepositSucceededPayload payload = event.getPayload();
        log.info("DepositSucceededEvent 처리: sellId={}", payload.getSellId());

        Orders sellOrder = ordersRepository.findById(payload.getSellId().intValue())
                .orElseThrow(() -> new IllegalArgumentException("판매 주문을 찾을 수 없습니다."));

        // ✅ 소유자 검증
        if (!sellOrder.getWalletAddress().equals(payload.getSellerAddress())) {
            throw new SecurityException("주문 소유자(지갑 주소)가 일치하지 않습니다.");
        }

        // ✅ 상태를 WAITING으로 변경
        sellOrder.setOrdersStatus("WAITING");
        // 토큰 개수는 이미 예치 시점에 확정되었으므로 여기서는 상태만 변경
        ordersRepository.save(sellOrder);
    }

    @Transactional
    public void handleDepositFailed(DepositFailedEvent event) {
        DepositFailedEvent.DepositFailedPayload payload = event.getPayload();
        log.info("DepositFailedEvent 처리: sellId={}", payload.getSellId());

        Orders sellOrder = ordersRepository.findById(payload.getSellId().intValue())
                .orElseThrow(() -> new IllegalArgumentException("판매 주문을 찾을 수 없습니다."));

        // ✅ 상태를 REJECTED로 변경
        sellOrder.setOrdersStatus("REJECTED");
        ordersRepository.save(sellOrder);
    }

    @Transactional
    public void handleTradeRequestAccepted(TradeRequestAcceptedEvent event) {
        TradeRequestAcceptedEvent.TradeRequestAcceptedPayload payload = event.getPayload();
        log.info("TradeRequestAcceptedEvent 처리: tradeId={}", payload.getTradeId());

        Trade trade = tradeRepository.findByTradeId(payload.getTradeId())
                .orElseThrow(() -> new IllegalArgumentException("거래를 찾을 수 없습니다."));

        // ✅ 상태를 PENDING으로 변경
        trade.setTradeStatus("PENDING");
        tradeRepository.save(trade);
    }

    @Transactional
    public void handleTradeRequestRejected(TradeRequestRejectedEvent event) {
        TradeRequestRejectedEvent.TradeRequestRejectedPayload payload = event.getPayload();
        log.info("TradeRequestRejectedEvent 처리: tradeId={}", payload.getTradeId());

        Trade trade = tradeRepository.findByTradeId(payload.getTradeId())
                .orElseThrow(() -> new IllegalArgumentException("거래를 찾을 수 없습니다."));

        // ✅ 명세에 따라 상태를 PENDING으로 변경
        // (참고: 일반적으로 REJECTED나 FAILED로 변경하는 것이 더 자연스러울 수 있습니다.)
        trade.setTradeStatus("PENDING");
        tradeRepository.save(trade);
    }

    @Transactional
    public void handleTradeSucceeded(TradeSucceededEvent event) {
        TradeSucceededEvent.TradeSucceededPayload payload = event.getPayload();
        log.info("TradeSucceededEvent 처리: tradeId={}", payload.getTradeId());

        Trade trade = tradeRepository.findByTradeId(payload.getTradeId())
                .orElseThrow(() -> new IllegalArgumentException("거래를 찾을 수 없습니다."));

        // ✅ 마켓 서비스는 '결과 보고'를 받고 자신의 DB 상태만 업데이트합니다.
        trade.setTradeStatus("SUCCEEDED");
        tradeRepository.save(trade);

        Orders purchaseOrder = ordersRepository.findById(trade.getPurchaseId().intValue()).orElseThrow();
        purchaseOrder.setOrdersStatus("SUCCEEDED");
        ordersRepository.save(purchaseOrder);

        Orders sellOrder = ordersRepository.findById(trade.getSellId().intValue()).orElseThrow();
        sellOrder.setOrdersStatus("SUCCEEDED");
        ordersRepository.save(sellOrder);

        // ❌ Asset 서비스를 직접 호출하여 돈을 보내는 로직 삭제!
        // 이 역할은 Escrow 서비스가 이미 수행 완료했습니다.
        log.info("거래 성공 DB 상태 업데이트 완료. tradeId={}", payload.getTradeId());
    }

    /**
     * (최종 결과) 거래 실패 이벤트를 처리합니다.
     * 실패 보고를 받고, 동결했던 구매자의 돈을 풀어주도록 Asset에 요청합니다.
     */
    @Transactional
    public void handleTradeFailed(TradeFailedEvent event) {
        TradeFailedEvent.TradeFailedPayload payload = event.getPayload();
        log.info("TradeFailedEvent 처리: tradeId={}", payload.getTradeId());

        Trade trade = tradeRepository.findByTradeId(payload.getTradeId())
                .orElseThrow(() -> new IllegalArgumentException("거래를 찾을 수 없습니다."));

        // ✅ 마켓 서비스는 DB 상태를 FAILED로 업데이트합니다.
        trade.setTradeStatus("FAILED");
        tradeRepository.save(trade);

        Orders purchaseOrder = ordersRepository.findById(trade.getPurchaseId().intValue()).orElseThrow();
        purchaseOrder.setOrdersStatus("FAILED");
        ordersRepository.save(purchaseOrder);

        Orders sellOrder = ordersRepository.findById(trade.getSellId().intValue()).orElseThrow();
        sellOrder.setOrdersStatus("FAILED");
        ordersRepository.save(sellOrder);

        // ✅ [수정] 판매자에게 돈을 보내는 대신, '실패'했으므로
        // 처음에 동결했던 구매자의 돈을 '동결 해제'하도록 Asset 서비스에 요청합니다.
        try {
            UnlockFundsRequestDto request = new UnlockFundsRequestDto(
                    String.valueOf(purchaseOrder.getOrdersId()),
                    purchaseOrder.getUserSeq()
            );
            assetClient.unlockFunds(request);
            log.info("거래 실패로 인한 자금 동결 해제 요청 완료. orderId={}", purchaseOrder.getOrdersId());
        } catch (Exception e) {
            log.error("Asset 서비스 호출(자금 동결 해제) 실패. tradeId={}", trade.getTradeId(), e);
            // TODO: 재시도 로직 또는 관리자 알림 등 복구 정책 필요
        }
    }
}
