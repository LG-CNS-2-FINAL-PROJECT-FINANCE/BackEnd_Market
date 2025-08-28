package com.ddiring.backend_market.investment.service;

import com.ddiring.backend_market.api.asset.AssetClient;
import com.ddiring.backend_market.api.asset.dto.request.MarketBuyDto;
import com.ddiring.backend_market.api.asset.dto.request.MarketRefundDto;
import com.ddiring.backend_market.api.blockchain.BlockchainClient;
import com.ddiring.backend_market.api.blockchain.dto.request.InvestmentDto;
import com.ddiring.backend_market.api.product.ProductClient;
import com.ddiring.backend_market.api.user.UserClient;
import com.ddiring.backend_market.api.product.ProductDTO;
import com.ddiring.backend_market.api.user.UserDTO;
import com.ddiring.backend_market.event.dto.InvestRequestEvent;
import com.ddiring.backend_market.event.producer.InvestmentEventProducer;
import com.ddiring.backend_market.investment.dto.request.CancelInvestmentRequest;
import com.ddiring.backend_market.investment.dto.request.InvestmentRequest;
import com.ddiring.backend_market.investment.dto.response.*;
import com.ddiring.backend_market.investment.entity.Investment;
import com.ddiring.backend_market.investment.entity.Investment.InvestmentStatus;
import com.ddiring.backend_market.investment.repository.InvestmentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class InvestmentService {

    private final InvestmentEventProducer investmentEventProducer;
    private final InvestmentRepository investmentRepository;
    private final UserClient userClient;
    private final ProductClient productClient;
    private final AssetClient assetClient;
    private final BlockchainClient blockchainClient; // 블록체인 연동 Client

    // 투자 상품 전체 조회
    public List<ProductDTO> getAllProduct() {
        return productClient.getAllProduct();
    }

    // 개인 투자 내역 조회
    public List<MyInvestmentResponse> getMyInvestment(String userSeq) {
        List<Investment> myList = investmentRepository.findByUserSeq(userSeq);

        if (myList.isEmpty()) {
            return List.of();
        }

        List<ProductDTO> allProducts;
        try {
            allProducts = productClient.getAllProduct();
        } catch (Exception e) {
            log.warn("상품 불러오기 실패. reason={}", e.getMessage());
            return myList.stream()
                    .map(investment -> MyInvestmentResponse.builder()
                            .product(null)
                            .investedPrice(investment.getInvestedPrice())
                            .tokenQuantity(investment.getTokenQuantity())
                            .build())
                    .toList();
        }

        Set<String> neededIds = myList.stream()
                .map(Investment::getProjectId)
                .collect(Collectors.toSet());

        Map<String, ProductDTO> productMap = allProducts.stream()
                .filter(p -> p != null && p.getProjectId() != null && neededIds.contains(p.getProjectId()))
                .collect(Collectors.toMap(
                        ProductDTO::getProjectId,
                        Function.identity(),
                        (a, b) -> a));

        return myList.stream()
                .map(investment -> {
                    ProductDTO product = productMap.get(investment.getProjectId());
                    return MyInvestmentResponse.builder()
                            .product(product)
                            .investedPrice(investment.getInvestedPrice())
                            .tokenQuantity(investment.getTokenQuantity())
                            .invStatus(investment.getInvStatus() == null ? null : investment.getInvStatus().name())
                            .build();
                })
                .toList();
    }

    // 상품 주문 확인
    public List<MyInvestmentByProductResponse> getMyInvestmentByProduct(String userSeq, String projectId) {
        List<Investment> myInvestments = investmentRepository.findByUserSeqAndProjectId(userSeq, projectId);

        return myInvestments.stream()
                .filter(inv -> inv.getInvStatus() == Investment.InvestmentStatus.FUNDING
                        || inv.getInvStatus() == Investment.InvestmentStatus.PENDING)
                .map(investment -> MyInvestmentByProductResponse.builder()
                        .investmentSeq(investment.getInvestmentSeq())
                        .investedPrice(investment.getInvestedPrice())
                        .tokenQuantity(investment.getTokenQuantity())
                        .investedAt(investment.getInvestedAt())
                        .invStatus(investment.getInvStatus())
                        .build())
                .toList();
    }

    // 상품별 투자자 조회
    public List<ProductInvestorResponse> getInvestorByProduct(String projectId) {
        List<Investment> investments = investmentRepository.findByProjectId(projectId);

        List<String> investorList = investments.stream()
                .map(Investment::getUserSeq)
                .map(u -> {
                    try {
                        return u;
                    } catch (Exception e) {
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        List<UserDTO> dto = userClient.getUser(investorList);

        return investments.stream()
                .map(i -> {
                    UserDTO user = dto.stream()
                            .filter(u -> u.getUserSeq().equals(i.getUserSeq()))
                            .findFirst()
                            .orElse(null);

                    return ProductInvestorResponse.builder()
                            .user(user)
                            .investedPrice(i.getInvestedPrice())
                            .tokenQuantity(i.getTokenQuantity())
                            .investedAt(i.getInvestedAt())
                            .build();
                })
                .toList();
    }

    // 주문
    @Transactional
    public InvestmentResponse buyInvestment(String userSeq, String role, InvestmentRequest request) {
        ProductDTO product = productClient.getProduct(request.getProjectId());

        Integer minIvestment = product.getMinInvestment();
        Integer investedPrice = request.getInvestedPrice();
        if (investedPrice < minIvestment) {
            throw new IllegalArgumentException("최소 투자 금액 미달");
        }

        int calcToken = investedPrice / minIvestment;

        Investment investment = Investment.builder()
                .userSeq(userSeq)
                .projectId(request.getProjectId())
                .investedPrice(request.getInvestedPrice())
                .tokenQuantity(calcToken)
                .investedAt(LocalDateTime.now())
                .invStatus(InvestmentStatus.PENDING)
                .build();

        Investment saved = investmentRepository.save(investment);

        MarketBuyDto marketBuyDto = new MarketBuyDto();
        marketBuyDto.setOrdersId(investment.getInvestmentSeq());
        marketBuyDto.setProjectId(investment.getProjectId());
        marketBuyDto.setBuyPrice(investment.getInvestedPrice());
        marketBuyDto.setTransType(0);

        try {
            assetClient.marketBuy(userSeq, role, marketBuyDto);
        } catch (Exception e) {
            saved.setInvStatus(InvestmentStatus.CANCELLED);
            saved.setUpdatedAt(LocalDateTime.now());
            investmentRepository.save(saved);

            return toResponse(saved);
        }

        // 입금 성공 => 펀딩 진행 상태로 변경
        saved.setInvStatus(InvestmentStatus.FUNDING);
        saved.setUpdatedAt(LocalDateTime.now());
        investmentRepository.save(saved);
        return toResponse(saved);
    }

    // 주문 취소
    @Transactional
    public InvestmentResponse cancelInvestment(String userSeq, String role, CancelInvestmentRequest request) {
        Investment investment = investmentRepository.findById(request.getInvestmentSeq())
                .orElseThrow(() -> new IllegalArgumentException("없는 주문입니다: " + request.getInvestmentSeq()));

        // 이미 취소된 경우 그대로 반환
        if (investment.isCancelled()) {
            return toResponse(investment);
        }

        // 1) PENDING -> 단순 취소
        if (investment.isPending()) {
            investment.setInvStatus(InvestmentStatus.CANCELLED);
            investment.setUpdatedAt(LocalDateTime.now());
            investmentRepository.save(investment); // 삭제 대신 상태만 변경하여 이력 유지
            return toResponse(investment);
        }

        // 2) FUNDING -> 환불 필요
        boolean requireRefund = switch (investment.getInvStatus()) {
            case FUNDING -> true;
            default -> false;
        };

        if (requireRefund) {
            MarketRefundDto marketRefundDto = new MarketRefundDto();
            marketRefundDto.setOrdersId(investment.getInvestmentSeq());
            marketRefundDto.setProjectId(investment.getProjectId());
            marketRefundDto.setRefundPrice(investment.getInvestedPrice());

            try {
                assetClient.marketRefund(userSeq, role, marketRefundDto);
            } catch (Exception e) {
                throw new IllegalStateException("환불 요청 실패");
            }
        }

        // 환불 성공 -> 상태 CANCELLED 후 삭제
        investment.setInvStatus(InvestmentStatus.CANCELLED);
        investment.setUpdatedAt(LocalDateTime.now());
        investmentRepository.save(investment);
        return toResponse(investment);
    }

    // 토큰 할당 요청 트리거
    @Transactional
    public boolean triggerAllocationIfEligible(String projectId) {
        ProductDTO product = productClient.getProduct(projectId);
        if (product == null) {
            log.warn("프로젝트 없음 projectId={}", projectId);
            return false;
        }

        Integer percent = product.getPercent();
        if (percent == null) {
            log.warn("percent 정보 없음 projectId={}", projectId);
            return false;
        }

        LocalDate endDate = product.getEndDate();
        if (endDate == null || LocalDate.now().isBefore(endDate)) {
            log.info("아직 종료일 이전 projectId={} today={} endDate={}", projectId, LocalDate.now(), endDate);
            return false;
        }

        if (percent < 80) {
            log.info("달성률 미달 projectId={} percent={}", projectId, percent);
            return false;
        }

        // FUNDING 상태 투자 조회
        List<Investment> funding = investmentRepository.findByProjectId(projectId).stream()
                .filter(inv -> inv.getInvStatus() == InvestmentStatus.FUNDING)
                .collect(Collectors.toList());

        if (funding.isEmpty()) {
            log.info("FUNDING 투자 없음 projectId={}", projectId);
            return false;
        }

        // 상태 전환 ALLOC_REQUESTED
        funding.forEach(inv -> inv.setInvStatus(InvestmentStatus.ALLOC_REQUESTED));
        investmentRepository.saveAll(funding);

        // 이벤트 생성 및 발행
        List<InvestRequestEvent.InvestmentItem> items = funding.stream()
                .map(inv -> InvestRequestEvent.InvestmentItem.builder()
                        .investmentSeq(inv.getInvestmentSeq())
                        .userSeq(inv.getUserSeq())
                        .investedPrice(inv.getInvestedPrice())
                        .tokenQuantity(inv.getTokenQuantity())
                        .build())
                .collect(Collectors.toList());

        InvestRequestEvent event = InvestRequestEvent.of(projectId, items);
        investmentEventProducer.send("INVEST", event);
        log.info("투자 요청 이벤트 발행 projectId={} investments={}", projectId, items.size());

        // 블록체인 토큰 이동 API 호출
        try {
            boolean bcRequested = requestBlockchainTokenMove(projectId);
            log.info("블록체인 토큰 이동 요청 수행 projectId={} requested={}", projectId, bcRequested);
        } catch (Exception e) {
            log.error("블록체인 토큰 이동 요청 중 예외 projectId={} reason={}", projectId, e.getMessage());
        }
        return true;
    }

    // 블록 체인 토큰 이동
    @Transactional
    public boolean requestBlockchainTokenMove(String projectId) {
        // ALLOC_REQUESTED 상태 투자 조회
        List<Investment> allocRequested = investmentRepository.findByProjectId(projectId).stream()
                .filter(inv -> inv.getInvStatus() == InvestmentStatus.ALLOC_REQUESTED)
                .toList();
        if (allocRequested.isEmpty()) {
            log.info("토큰 이동 대상 없음 projectId={}", projectId);
            return false;
        }

        List<InvestmentDto.InvestInfo> investInfoList = new ArrayList<>();
        for (Investment inv : allocRequested) {
            try {
                // 지갑 주소 조회
                String address = assetClient.getWalletAddress(inv.getUserSeq()).getData();
                if (address == null || address.isBlank()) {
                    inv.setInvStatus(InvestmentStatus.FAILED);
                    inv.setFailureReason("NO_WALLET_ADDRESS");
                    inv.setUpdatedAt(LocalDateTime.now());
                    continue;
                }
                investInfoList.add(InvestmentDto.InvestInfo.builder()
                        .investmentId(inv.getInvestmentSeq().longValue())
                        .investorAddress(address)
                        .tokenAmount(inv.getTokenQuantity().longValue())
                        .build());
            } catch (Exception e) {
                inv.setInvStatus(InvestmentStatus.FAILED);
                inv.setFailureReason("ADDR_ERR:" + e.getClass().getSimpleName());
                inv.setUpdatedAt(LocalDateTime.now());
            }
        }
        investmentRepository.saveAll(allocRequested); // 주소 실패 건 반영

        if (investInfoList.isEmpty()) {
            log.warn("유효한 투자 없음 projectId={} -> 블록체인 요청 생략", projectId);
            return false;
        }

        InvestmentDto investmentDto = InvestmentDto.builder()
                .projectId(projectId)
                .investInfoList(investInfoList)
                .build();

        try {
            blockchainClient.requestInvestmentTokenMove(investmentDto);
            log.info("블록체인 토큰 이동 요청 전송 projectId={} count={}", projectId, investInfoList.size());
            return true;
        } catch (Exception e) {
            log.error("블록체인 토큰 이동 요청 실패 projectId={} reason={}", projectId, e.getMessage());
            allocRequested.stream()
                    .filter(inv -> inv.getInvStatus() == InvestmentStatus.ALLOC_REQUESTED)
                    .forEach(inv -> {
                        inv.setInvStatus(InvestmentStatus.FAILED);
                        inv.setFailureReason("BC_REQ_FAIL:" + e.getClass().getSimpleName());
                        inv.setUpdatedAt(LocalDateTime.now());
                    });
            investmentRepository.saveAll(allocRequested);
            return false;
        }
    }

    private InvestmentResponse toResponse(Investment inv) {
        return InvestmentResponse.builder()
                .investmentSeq(inv.getInvestmentSeq())
                .userSeq(inv.getUserSeq())
                .projectId(inv.getProjectId())
                .investedPrice(inv.getInvestedPrice())
                .tokenQuantity(inv.getTokenQuantity())
                .invStatus(inv.getInvStatus().name())
                .investedAt(inv.getInvestedAt())
                .build();
    }
}
