package com.ddiring.backend_market.trade.service;

import com.ddiring.backend_market.common.exception.BadParameter;
import com.ddiring.backend_market.common.exception.NotFound;
import com.ddiring.backend_market.trade.dto.OrdersResponseDto;
import com.ddiring.backend_market.trade.dto.OrderHistoryResponseDto;
import com.ddiring.backend_market.trade.dto.TradeHistoryResponseDto;
import com.ddiring.backend_market.trade.dto.TradeSearchResponseDto;
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

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TradeService {
    private final OrdersRepository ordersRepository;
    private final TradeRepository tradeRepository;
    private final HistoryRepository historyRepository;

    private void matchAndExecuteTrade(Orders order, List<Orders> oldOrders) {
        for (Orders oldOrder : oldOrders) {
            boolean tradePossible = false;
            if (order.getOrdersType() == 1 && order.getPurchasePrice() >= oldOrder.getPurchasePrice()) {
                tradePossible = true;
            }
            else if (order.getOrdersType() == 0 && order.getPurchasePrice() <= oldOrder.getPurchasePrice()) {
                tradePossible = true;
            }

            if (tradePossible) {
                int tradedQuantity = Math.min(order.getTokenQuantity(), oldOrder.getTokenQuantity());
                int tradePrice = order.getOrdersType() == 1 ? oldOrder.getPurchasePrice() : order.getPurchasePrice();

                Trade trade = Trade.builder()
                        .projectId(order.getProjectId())
                        .purchaseId(order.getOrdersType() == 1 ? order.getOrdersId() : oldOrder.getOrdersId())
                        .sellId(order.getOrdersType() == 0 ? order.getOrdersId() : oldOrder.getOrdersId())
                        .tradePrice(tradePrice)
                        .tokenQuantity(tradedQuantity)
                        .tradedAt(LocalDate.now())
                        .build();

                tradeRepository.save(trade);

                History purchaseHistory = History.builder()
                        .projectId(order.getProjectId())
                        .userSeq(order.getOrdersType() == 1 ? order.getUserSeq() : oldOrder.getUserSeq())
                        .tradeType(1)
                        .tradePrice(tradePrice)
                        .tokenQuantity(tradedQuantity)
                        .tradedAt(LocalDate.now())
                        .build();
                historyRepository.save(purchaseHistory);

                History sellHistory = History.builder()
                        .projectId(order.getProjectId())
                        .userSeq(order.getOrdersType() == 0 ? order.getUserSeq() : oldOrder.getUserSeq())
                        .tradeType(0)
                        .tradePrice(tradePrice)
                        .tokenQuantity(tradedQuantity)
                        .tradedAt(LocalDate.now())
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
        }
    }

    @Transactional
    public void OrderReception(String userSeq, String projectId, Integer purchasePrice, Integer tokenQuantity, Integer ordersType) {
        if (userSeq == null || projectId == null || purchasePrice == null || tokenQuantity < 0) {
            throw new BadParameter("값 없음 넣으셈");
        }
        Orders order = Orders.builder()
                .userSeq(userSeq)
                .projectId(projectId)
                .ordersType(ordersType)
                .purchasePrice(purchasePrice)
                .tokenQuantity(tokenQuantity)
                .registedAt(LocalDate.now())
                .build();
        Orders savedOrder = ordersRepository.save(order);

        if(ordersType == 1) {
            List<Orders> sellOrder = ordersRepository.findByProjectIdAndOrdersTypeOrderByPurchasePriceAscRegistedAtAsc(projectId, 0);
            matchAndExecuteTrade(savedOrder, sellOrder); // 저장된 객체를 전달
        }
        else {
            List<Orders> purchaseOrder = ordersRepository.findByProjectIdAndOrdersTypeOrderByPurchasePriceDescRegistedAtAsc(projectId, 1);
            matchAndExecuteTrade(savedOrder, purchaseOrder);
        }

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
}
