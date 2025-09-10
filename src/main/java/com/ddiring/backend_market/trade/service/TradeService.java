package com.ddiring.backend_market.trade.service;

import com.ddiring.backend_market.api.asset.AssetClient;
import com.ddiring.backend_market.api.asset.dto.request.*;
import com.ddiring.backend_market.api.asset.dto.request.MarketRefundDto;
import com.ddiring.backend_market.api.blockchain.BlockchainClient;
import com.ddiring.backend_market.api.blockchain.dto.signature.PermitSignatureDto;
import com.ddiring.backend_market.api.blockchain.dto.trade.*;
import com.ddiring.backend_market.api.user.UserClient;
import com.ddiring.backend_market.common.dto.ApiResponseDto;
import com.ddiring.backend_market.common.exception.BadParameter;
import com.ddiring.backend_market.common.exception.NotFound;
import com.ddiring.backend_market.event.dto.TradeFailedEvent;
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
import org.web3j.crypto.Sign;
import org.web3j.utils.Numeric;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.Arrays;
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
    private final TradeEventProducer tradeEventProducer;
    private final BlockchainClient blockchainClient;
    private final SignatureService signatureService;

    private void matchAndExecuteTrade(Orders order, List<Orders> oldOrders) {
        for (Orders oldOrder : oldOrders) {
            if (!"SUCCEEDED".equals(oldOrder.getOrdersStatus())) continue;
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
                        .purchaseId(order.getOrdersType() == 1 ? order.getOrdersId() : oldOrder.getOrdersId())
                        .sellId(order.getOrdersType() == 0 ? order.getOrdersId() : oldOrder.getOrdersId())
                        .buyerAddress(order.getOrdersType() == 1 ? order.getWalletAddress() : oldOrder.getWalletAddress())
                        .sellerAddress(order.getOrdersType() == 0 ? order.getWalletAddress() : oldOrder.getWalletAddress())
                        .tradePrice(tradePrice * tradedQuantity)
                        .tokenPerPrice(tradePrice)
                        .tokenQuantity(tradedQuantity)
                        .tradedAt(LocalDateTime.now())
                        .tradeStatus("PENDING")
                        .build();

                tradeRepository.save(trade);

                BuyInfo buyInfo = BuyInfo.builder()
                        .buyId(Long.valueOf(order.getOrdersType() == 1 ? order.getOrdersId() : oldOrder.getOrdersId()))
                        .buyerAddress(order.getOrdersType() == 1 ? order.getWalletAddress() : oldOrder.getWalletAddress())
                        .build();

                SellInfo sellInfo = SellInfo.builder()
                        .sellId(Long.valueOf(order.getOrdersType() == 0 ? order.getOrdersId() : oldOrder.getOrdersId()))
                        .sellerAddress(order.getOrdersType() == 0 ? order.getWalletAddress() : oldOrder.getWalletAddress())
                        .build();

                TradeDto tradeDto = TradeDto.builder()
                        .tradeId(trade.getTradeId())
                        .projectId(order.getProjectId())
                        .buyInfo(buyInfo)
                        .sellInfo(sellInfo)
                        .tradeAmount((long) tradedQuantity)
                        .pricePerToken((long) (tradePrice / tradedQuantity))
                        .build();


                try {
                    blockchainClient.requestTradeTokenMove(tradeDto);
                } catch (Exception e) {
                    log.error("블록체인 통신 실패. Trade ID: {}", trade.getTradeId(), e);
                    // 블록체인 통신 실패 시 보상 트랜잭션 이벤트 발행
                    tradeEventProducer.send(TradeFailedEvent.TOPIC, TradeFailedEvent.of(
                            trade.getProjectId(),
                            trade.getTradeId(),
                            trade.getBuyerAddress(),
                            trade.getSellerAddress(),
                            (long) trade.getTokenQuantity(),
                            "BLOCKCHAIN_ERROR",
                            e.getMessage()
                    ));
                    // 거래 상태 FAILED로 변경
                    trade.setTradeStatus("FAILED");
                    tradeRepository.save(trade);

                    // 연관된 주문들도 보상 처리 (환불)
                    buyOrderRefund(order.getOrdersType() == 1 ? order.getOrdersId() : oldOrder.getOrdersId());
                    sellOrderRefund(order.getOrdersType() == 0 ? order.getOrdersId() : oldOrder.getOrdersId());

                    return; // 매칭 중단
                }

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

            else{
                    log.info("바보임?");
                log.info("거래 체결 실패. 구매 주문 ID: {}, 판매 주문 ID: {}", (order.getOrdersType() == 1 ? order.getUserSeq() : oldOrder.getUserSeq()), (order.getOrdersType() == 0 ? order.getUserSeq() : oldOrder.getUserSeq()));


            }

        }
    }

    @Transactional
    public Long sellReception(String userSeq, String role, OrdersRequestDto ordersRequestDto) {
        if (userSeq == null || ordersRequestDto.getProjectId() == null || ordersRequestDto.getOrdersType() == null
                || ordersRequestDto.getTokenQuantity() <= 0 || role == null) {
            throw new BadParameter("필수 파라미터가 누락되었습니다.");
        }
        if (ordersRequestDto.getOrdersType() == 1) {
            throw new BadParameter("이거 아이다 다른거 줘라");
        }

        MarketSellDto sellCheckDto = new MarketSellDto();
        sellCheckDto.setProjectId(ordersRequestDto.getProjectId());
        sellCheckDto.setSellToken(ordersRequestDto.getTokenQuantity());
        ApiResponseDto<Boolean> tokenCheckResponse = assetClient.checkToken(userSeq, sellCheckDto);
        if (tokenCheckResponse == null || !Boolean.TRUE.equals(tokenCheckResponse.getData())) {
            throw new BadParameter("판매할 토큰이 부족합니다.");
        }

        // 토큰을 즉시 차감하여 주문에 대한 토큰을 예약
        MarketSellDto sellDto = new MarketSellDto();
        sellDto.setTransType(0); // 판매 타입
        sellDto.setProjectId(ordersRequestDto.getProjectId());
        sellDto.setSellToken(ordersRequestDto.getTokenQuantity());

        try {
            ApiResponseDto<String> sellResponse = assetClient.marketSell(userSeq, sellDto);
            if (sellResponse == null || sellResponse.getData() == null) {
                throw new BadParameter("토큰 차감 처리에 실패했습니다.");
            }
            log.info("판매 주문 접수: 토큰 차감 완료. userSeq={}, projectId={}, tokenQuantity={}",
                    userSeq, ordersRequestDto.getProjectId(), ordersRequestDto.getTokenQuantity());
        } catch (Exception e) {
            log.error("토큰 차감 실패: userSeq={}, projectId={}, tokenQuantity={}",
                    userSeq, ordersRequestDto.getProjectId(), ordersRequestDto.getTokenQuantity(), e);
            throw new BadParameter("토큰 차감에 실패했습니다: " + e.getMessage());
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
                .ordersStatus("PENDING")
                .walletAddress(walletAddress)
                .registedAt(LocalDateTime.now())
                .build();

        Orders savedOrder = ordersRepository.save(order);

        // MarketSellDto에 주문 ID 설정 (차감된 토큰과 주문을 연결하기 위해)
        sellDto.setOrdersId(savedOrder.getOrdersId());

        tradeEventProducer.send("SELL_ORDER_INITIATED", savedOrder);
        log.info("판매 주문 Saga 시작. 주문 ID: {}", savedOrder.getOrdersId());

        try {

            PermitSignatureDto.Request permitRequest = PermitSignatureDto.Request.builder()
                    .projectId(ordersRequestDto.getProjectId())
                    .userAddress(walletAddress)
                    .tokenAmount((long) ordersRequestDto.getTokenQuantity())
                    .build();

            ApiResponseDto<PermitSignatureDto.Response> signatureDataResponse = blockchainClient.requestPermitSignature(permitRequest);
            PermitSignatureDto.Response dataToSign = signatureDataResponse.getData();

            if (dataToSign == null) {
                throw new IllegalStateException("Blockchain 서비스로부터 서명 데이터를 받지 못했습니다.");
            }

            Sign.SignatureData signature = signatureService.signPermit(userSeq, dataToSign);
            log.info("판매 주문 ID {}에 대한 서버 서명 및 제출 완료", savedOrder.getOrdersId());

            byte[] v_bytes = signature.getV();
            byte[] r_bytes = signature.getR();
            byte[] s_bytes = signature.getS();

            Integer v = (int) v_bytes[0];
            String r = Numeric.toHexString(r_bytes);
            String s = Numeric.toHexString(s_bytes);

            BigInteger deadline = dataToSign.getMessage().getDeadline();
            DepositDto depositDto = DepositDto.builder()
                    .projectId(ordersRequestDto.getProjectId())
                    .sellerAddress(walletAddress)
                    .sellId(Long.valueOf(order.getOrdersId()))
                    .tokenAmount(BigInteger.valueOf(ordersRequestDto.getTokenQuantity()))
                    .deadline(deadline)
                    .v(v)
                    .r(r)
                    .s(s)
                    .build();
            blockchainClient.requestDeposit(depositDto);
            log.info("판매 주문 ID {}에 대한 서명 생성 및 Deposit 요청 완료", order.getOrdersId());
        } catch (Exception e) {
            log.error("판매 주문 ID {}에 대한 서버 서명 실패: {}", savedOrder.getOrdersId(), e.getMessage(), e);

            // 블록체인 처리 실패 시 차감된 토큰을 환불
            try {
                MarketRefundDto rollbackRefundDto = new MarketRefundDto();
                rollbackRefundDto.setOrdersId(savedOrder.getOrdersId());
                rollbackRefundDto.setProjectId(ordersRequestDto.getProjectId());
                rollbackRefundDto.setRefundAmount(ordersRequestDto.getTokenQuantity());
                rollbackRefundDto.setOrderType(0); // 판매 주문
                assetClient.marketRefund(userSeq, role, rollbackRefundDto);
                log.info("블록체인 처리 실패로 인한 토큰 롤백 완료. 주문 ID: {}", savedOrder.getOrdersId());
            } catch (Exception rollbackException) {
                log.error("토큰 롤백 실패. 주문 ID: {}", savedOrder.getOrdersId(), rollbackException);
            }

            // 실패한 주문 삭제
            ordersRepository.delete(savedOrder);

            throw new RuntimeException("블록체인 서명 처리에 실패했습니다.", e);
        }

        return (long)savedOrder.getOrdersId();
    }

    @Transactional
    public void sellOrderRefund(Integer orderId) {
        ordersRepository.findByOrdersId(orderId).ifPresent(order -> {
            if ("PENDING".equals(order.getOrdersStatus())) {
                log.info("판매 주문 보상 트랜잭션 시작. 주문 ID: {}", orderId);

                // 블록체인 예치 실패 시에만 토큰 환불 (이미 차감되었기 때문에)
                MarketRefundDto marketRefundDto = new MarketRefundDto();
                marketRefundDto.setOrdersId(order.getOrdersId());
                marketRefundDto.setProjectId(order.getProjectId());
                marketRefundDto.setRefundAmount(order.getTokenQuantity());
                marketRefundDto.setOrderType(order.getOrdersType());
                assetClient.marketRefund(order.getUserSeq(), order.getRole(), marketRefundDto);

                log.info("판매 주문 토큰 환불 완료. 주문 ID: {}, 환불 토큰 수량: {}",
                        orderId, order.getTokenQuantity());

                ordersRepository.delete(order);
                log.info("판매 주문 삭제 완료. 주문 ID: {}", orderId);
            }
        });
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

        MarketBuyDto buyCheckDto = new MarketBuyDto();
        buyCheckDto.setBuyPrice(ordersRequestDto.getPurchasePrice() * ordersRequestDto.getTokenQuantity());
        ApiResponseDto<Boolean> balanceCheckResponse = assetClient.checkBalance(userSeq, role, buyCheckDto);
        if (balanceCheckResponse == null || !Boolean.TRUE.equals(balanceCheckResponse.getData())) {
            throw new BadParameter("잔액이 부족합니다.");
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
                .perPrice(ordersRequestDto.getPurchasePrice())
                .purchasePrice(ordersRequestDto.getPurchasePrice() * ordersRequestDto.getTokenQuantity())
                .tokenQuantity(ordersRequestDto.getTokenQuantity())
                .ordersStatus("PENDING")
                .registedAt(LocalDateTime.now())
                .build();

        Orders savedOrder = ordersRepository.save(order);

        tradeEventProducer.send("BUY_ORDER_INITIATED", savedOrder);
        log.info("구매 주문 Saga 시작. 주문 ID: {}", savedOrder.getOrdersId());
        logSales(userSeq, true, savedOrder.getOrdersId(), ordersRequestDto);

    }

    @Transactional
    public void buyOrderRefund(Integer orderId) {
        ordersRepository.findByOrdersId(orderId).ifPresent(order -> {
            if ("PENDING".equals(order.getOrdersStatus())) {
                log.info("구매 주문 보상 트랜잭션 시작. 주문 ID: {}", orderId);
                // Asset 서비스에 환불 요청
                MarketRefundDto marketRefundDto = new MarketRefundDto();
                marketRefundDto.setOrdersId(order.getOrdersId());
                marketRefundDto.setProjectId(order.getProjectId());
                marketRefundDto.setRefundPrice(order.getPurchasePrice());
                marketRefundDto.setRefundAmount(order.getTokenQuantity());
                marketRefundDto.setOrderType(order.getOrdersType()); // 구매 타입
                assetClient.marketRefund(order.getUserSeq(), order.getRole(), marketRefundDto);


                ordersRepository.delete(order);
                log.info("구매 주문 상태 FAILED로 변경. 주문 ID: {}", orderId);
            }
        });
    }

    @Transactional
    public void beforeMatch(Orders order) {
        if (order == null || !"SUCCEEDED".equals(order.getOrdersStatus())) {
            log.warn("거래 체결을 시작할 수 없습니다. 주문이 없거나 상태가 SUCCEEDED가 아닙니다. order: {}", order);
            return;
        }

        log.info("주문 ID {}에 대한 거래 체결 로직을 시작합니다.", order.getOrdersId());

        List<Orders> orders;
        if (order.getOrdersType() == 1) {
            orders = ordersRepository
                    .findByProjectIdAndOrdersTypeAndOrdersStatusOrderByPurchasePriceAscRegistedAtAsc(
                            order.getProjectId(), 0, "SUCCEEDED");
            if (orders.isEmpty()) {
                log.info("주문 ID {}에 대한 거래 상대방(판매 주문)이 없습니다.", order.getOrdersId());
                return;
            }
        }
        else {
            orders = ordersRepository
                    .findByProjectIdAndOrdersTypeAndOrdersStatusOrderByPurchasePriceDescRegistedAtAsc(
                            order.getProjectId(), 1, "SUCCEEDED");
            if (orders.isEmpty()) {
                log.info("주문 ID {}에 대한 거래 상대방(구매 주문)이 없습니다.", order.getOrdersId());
                return;
            }
        }

        matchAndExecuteTrade(order, orders);
    }
    @Transactional
    public void deleteOrder(String userSeq, String role, OrderDeleteDto orderDeleteDto) {
        if (orderDeleteDto.getOrderId() == null || userSeq == null || role == null) {
            throw new BadParameter("필요한 거 누락되었습니다.");
        }

        Orders order = ordersRepository.findByOrdersId(orderDeleteDto.getOrderId())
                .orElseThrow(() -> new NotFound("권한 가져와"));

        if (order.getOrdersType() == 0) {
            PermitSignatureDto.Request permitRequest = PermitSignatureDto.Request.builder()
                    .projectId(order.getProjectId())
                    .userAddress(order.getWalletAddress())
                    .tokenAmount((long) order.getTokenQuantity())
                    .build();
            ApiResponseDto<PermitSignatureDto.Response> signatureDataResponse = blockchainClient.requestPermitSignature(permitRequest);
            PermitSignatureDto.Response dataToSign = signatureDataResponse.getData();

            Sign.SignatureData signature = signatureService.signPermit(userSeq, dataToSign);
            log.info("판매 주문 취소 ID {}에 대한 서버 서명 및 제출 완료", orderDeleteDto.getOrderId());

            byte[] v_bytes = signature.getV();
            byte[] r_bytes = signature.getR();
            byte[] s_bytes = signature.getS();

            Integer v = (int) v_bytes[0];
            String r = Numeric.toHexString(r_bytes);
            String s = Numeric.toHexString(s_bytes);

            BigInteger deadline = dataToSign.getMessage().getDeadline();
            DepositDto depositDto = DepositDto.builder()
                    .projectId(order.getProjectId())
                    .sellerAddress(order.getWalletAddress())
                    .sellId(Long.valueOf(order.getOrdersId()))
                    .tokenAmount(BigInteger.valueOf(order.getTokenQuantity()))
                    .deadline(deadline)
                    .v(v)
                    .r(r)
                    .s(s)
                    .build();

            try {
                blockchainClient.requestDepositCancel(depositDto);
                log.info("판매 주문 ID {}에 대한 블록체인 취소 요청 완료", order.getOrdersId());
            } catch (Exception e) {
                log.error("주문 ID {} 블록체인 취소 요청 실패: {}", order.getOrdersId(), e.getMessage());
            }
        }

            MarketRefundDto marketRefundDto = new MarketRefundDto();
            marketRefundDto.setOrdersId(orderDeleteDto.getOrderId());
            marketRefundDto.setProjectId(order.getProjectId());
            marketRefundDto.setRefundPrice(order.getPurchasePrice());
            marketRefundDto.setRefundAmount(order.getTokenQuantity());
            marketRefundDto.setOrderType(order.getOrdersType());
            assetClient.marketRefund(userSeq, role, marketRefundDto);
            log.info("환불 가격 : {}", order.getPurchasePrice());

            ordersRepository.delete(order);
        log.info("주문 삭제: 삭제 주문 ID: {}, 프로젝트 ID {}, 주문 번호: {}", orderDeleteDto.getOrderId(), order.getProjectId(), orderDeleteDto.getOrderId());
        try {
            MDC.put("userSeq", userSeq);
            MDC.put("orderId", orderDeleteDto.toString());
            MDC.put("projectId", order.getProjectId());
            MDC.put("purchasePrice", order.getPurchasePrice().toString());
            MDC.put("tokenQuantity", order.getTokenQuantity().toString());
            MDC.put("ordersType", order.getOrdersType().toString());
            log.info("delete log captured.");
        }
        finally {
            MDC.clear();
        }
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
                requestDto.getBuyId(),
                requestDto.getSellId(),
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
                .price(trade.getTradePrice())
                .tokenQuantity(trade.getTokenQuantity())
                .buyerUserSeq(trade.getPurchaseId())
                .sellerUserSeq(trade.getSellId())
                .status(trade.getTradeStatus())
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