package com.ddiring.backend_market.service;

import com.ddiring.backend_market.common.exception.BadParameter;
import com.ddiring.backend_market.common.exception.NotFound;
import com.ddiring.backend_market.dto.OrdersResponseDto;
import com.ddiring.backend_market.dto.TradeHistoryResponseDto;
import com.ddiring.backend_market.dto.TradeSearchResponseDto;
import com.ddiring.backend_market.entity.Orders;
import com.ddiring.backend_market.entity.Trade;
import com.ddiring.backend_market.repository.OrdersRepository;
import com.ddiring.backend_market.repository.TradeRepository;
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
    public void OrderReception(Integer userSeq, Integer projectId, Integer purchasePrice, Integer tokenQuantity, Integer ordersType) {
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
    public void updateOrder(Integer ordersId, Integer userSeq, Integer projectId, Integer purchasePrice, Integer tokenQuantity, Integer ordersType) {
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
        ordersRepository.save(order);
    }

    @Transactional
    public List<TradeHistoryResponseDto> getTradeHistory(Integer projectId) {
        if (projectId == null) {
            throw new BadParameter("번호 내놔");
        }
        List<Trade> trades = tradeRepository.findTop20ByProjectIdOrderByTradedAtDesc(projectId);
        return trades.stream()
                .map(TradeHistoryResponseDto::new)
                .collect(Collectors.toList());
    }

    @Transactional
    public List<OrdersResponseDto> getPurchaseOrders(Integer projectId) {
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
    public List<OrdersResponseDto> getSellOrders(Integer projectId) {
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
    public List<TradeSearchResponseDto> getUserInfo(Integer userId) {
        if (userId == null) {
            throw new BadParameter("넌 누구냐");
        }
        List<Orders> ordersHistory = ordersRepository.findByUserSeq(userId);

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
}
