package com.ddiring.backend_market.investment.service;

import com.ddiring.backend_market.api.asset.AssetClient;
import com.ddiring.backend_market.api.asset.dto.request.MarketBuyDto;
import com.ddiring.backend_market.api.asset.dto.request.MarketDto;
import com.ddiring.backend_market.api.asset.dto.request.MarketTokenDto;
import com.ddiring.backend_market.common.dto.ApiResponseDto;
import com.ddiring.backend_market.api.asset.dto.request.MarketRefundDto;
import com.ddiring.backend_market.api.blockchain.BlockchainClient;
import com.ddiring.backend_market.api.blockchain.dto.request.InvestmentDto;
import com.ddiring.backend_market.api.product.ProductClient;
import com.ddiring.backend_market.api.user.UserClient;
import com.ddiring.backend_market.api.product.ProductDTO;
import com.ddiring.backend_market.api.product.ProductDetailDTO;
import com.ddiring.backend_market.api.user.UserDTO;
import com.ddiring.backend_market.event.dto.InvestRequestEvent;
import com.ddiring.backend_market.event.producer.InvestmentEventProducer;
import com.ddiring.backend_market.investment.dto.VerifyInvestmentDto;
import com.ddiring.backend_market.investment.dto.request.CancelInvestmentRequest;
import com.ddiring.backend_market.investment.dto.request.InvestmentRequest;
import com.ddiring.backend_market.investment.dto.response.*;
import com.ddiring.backend_market.investment.entity.Investment;
import com.ddiring.backend_market.investment.entity.Investment.InvestmentStatus;
import com.ddiring.backend_market.investment.repository.InvestmentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.ResponseEntity;
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
    private final BlockchainClient blockchainClient;

    // 투자 상품 전체 조회
    public List<ProductDTO> getAllProduct() {
        try {
            return Optional.ofNullable(productClient.getAllProduct())
                    .map(ResponseEntity::getBody)
                    .orElse(List.of());
        } catch (Exception e) {
            log.error("[PRODUCT] 전체 조회 실패: {}", e.getMessage());
            return List.of();
        }
    }

    // 개인 투자 내역 조회
    public List<MyInvestmentResponse> getMyInvestment(String userSeq) {
        // 투자 중인 상품 목록 조회
        List<Investment> myList = investmentRepository.findByUserSeq(userSeq).stream()
                .filter(inv -> inv.getInvStatus() == InvestmentStatus.FUNDING
                        || inv.getInvStatus() == InvestmentStatus.PENDING)
                .toList();
        if (myList.isEmpty()) {
            return List.of();
        }

        // 한 상품의 투자 내역 그룹화
        Map<String, List<Investment>> grouped = myList.stream()
                .collect(Collectors.groupingBy(Investment::getProjectId));
        Set<String> neededIds = grouped.keySet();

        List<ProductDTO> allProducts;
        try {
            allProducts = Optional.ofNullable(productClient.getAllUnOpenProduct())
                    .map(ResponseEntity::getBody)
                    .orElse(List.of());
        } catch (Exception e) {
            log.warn("상품 불러오기 실패. reason={}", e.getMessage());
            return grouped.entrySet().stream()
                    .map(e2 -> {
                        int totalInvest = e2.getValue().stream().mapToInt(Investment::getInvestedPrice).sum();
                        int totalTokens = e2.getValue().stream().mapToInt(Investment::getTokenQuantity).sum();
                        return MyInvestmentResponse.builder()
                                .product(null)
                                .investedPrice(totalInvest)
                                .tokenQuantity(totalTokens)
                                .invStatus(InvestmentStatus.FUNDING.name())
                                .build();
                    })
                    .toList();
        }

        Map<String, ProductDTO> productMap = allProducts.stream()
                .filter(p -> p != null && p.getProjectId() != null && neededIds.contains(p.getProjectId()))
                .collect(Collectors.toMap(
                        ProductDTO::getProjectId,
                        Function.identity(),
                        (a, b) -> a));

        return grouped.entrySet().stream()
                .map(entry -> {
                    String projectId = entry.getKey();
                    List<Investment> list = entry.getValue();
                    int totalInvest = list.stream().mapToInt(Investment::getInvestedPrice).sum();
                    int totalTokens = list.stream().mapToInt(Investment::getTokenQuantity).sum();
                    LocalDateTime latest = list.stream()
                            .map(Investment::getInvestedAt)
                            .filter(Objects::nonNull)
                            .max(LocalDateTime::compareTo)
                            .orElse(null);
                    return new AbstractMap.SimpleEntry<>(latest, MyInvestmentResponse.builder()
                            .product(productMap.get(projectId))
                            .investedPrice(totalInvest)
                            .tokenQuantity(totalTokens)
                            .invStatus(InvestmentStatus.FUNDING.name())
                            .build());
                })
                .sorted((a, b) -> {
                    if (a.getKey() == null && b.getKey() == null)
                        return 0;
                    if (a.getKey() == null)
                        return 1;
                    if (b.getKey() == null)
                        return -1;
                    return b.getKey().compareTo(a.getKey());
                })
                .map(AbstractMap.SimpleEntry::getValue)
                .toList();
    }

    // 상품 주문 확인
    public List<MyInvestmentByProductResponse> getMyInvestmentByProduct(String userSeq, String projectId) {
        List<Investment> myInvestments = investmentRepository.findByUserSeqAndProjectId(userSeq, projectId);

        return myInvestments.stream()
                .filter(inv -> inv.getInvStatus() == InvestmentStatus.FUNDING
                        || inv.getInvStatus() == InvestmentStatus.PENDING)
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
    public InvestmentResponse buyInvestment(String projectId, String userSeq, String role, InvestmentRequest request) {
        ProductDetailDTO product = Optional.ofNullable(productClient.getProduct(projectId))
                .map(ResponseEntity::getBody)
                .orElseThrow(() -> new IllegalStateException("상품 정보를 가져올 수 없습니다."));

        if (product.getUserSeq().equals(userSeq)) {
            throw new IllegalStateException("자신이 등록한 상품에는 투자할 수 없습니다.");
        }

        if (product.getStartDate().isAfter(LocalDate.now())) {
            throw new IllegalStateException("해당 상품은 투자 기간이 아닙니다.");
        }

        if (product.getEndDate().isBefore(LocalDate.now())) {
            throw new IllegalStateException("투자가 종료된 상품입니다.");
        }

        Integer maxInvestment = product.getGoalAmount() - product.getAmount();
        Integer minIvestment = product.getMinInvestment();
        Integer investedPrice = request.getInvestedPrice();

        if (investedPrice < minIvestment) {
            throw new IllegalArgumentException("최소 투자 금액 미달");
        }
        if (investedPrice > maxInvestment) {
            throw new IllegalArgumentException("목표 금액 초과");
        }

        int calcToken = investedPrice / minIvestment;

        Investment investment = Investment.builder()
                .userSeq(userSeq)
                .projectId(projectId)
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
            CancelInvestmentRequest cancelInvestmentRequest = new CancelInvestmentRequest();
            cancelInvestmentRequest.setInvestmentSeq(investment.getInvestmentSeq());
            cancelInvestmentRequest.setProjectId(investment.getProjectId());
            cancelInvestmentRequest.setInvestedPrice(investment.getInvestedPrice());
            cancelInvestment(userSeq, role, cancelInvestmentRequest);
        }

        // 입금 성공 => 펀딩 진행 상태로 변경
        saved.setInvStatus(InvestmentStatus.FUNDING);
        saved.setUpdatedAt(LocalDateTime.now());
        investmentRepository.save(saved);

        log.info("Investment successful: userSeq={}", userSeq);
        log.info("projectId: {}", projectId);
        MarketTokenDto marketTokenDto = MarketTokenDto.builder()
                .perPrice(minIvestment)
                .tokenQuantity(calcToken)
                .userSeq(userSeq)
                .build();

        log.info("MarketTokenDto: {}", marketTokenDto);
        assetClient.getToken(projectId, marketTokenDto);

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
            marketRefundDto.setRefundAmount(investment.getTokenQuantity());
            marketRefundDto.setOrderType(2);

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
        ProductDetailDTO product = Optional.ofNullable(productClient.getProduct(projectId))
                .map(org.springframework.http.ResponseEntity::getBody)
                .orElse(null);
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
        investmentEventProducer.send("INVESTMENT", event);
        log.info("투자 요청 이벤트 발행 projectId={} investments={}", projectId, items.size());

        return true;
    }

    // 블록 체인 토큰 이동
    @Transactional
    public boolean requestBlockchainTokenMove(String projectId) {
        // ALLOC_REQUESTED 상태 투자 조회
        List<Investment> allocRequested = investmentRepository.findByProjectId(projectId).stream()
                .filter(inv -> inv.getInvStatus() == InvestmentStatus.FUNDING)
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
                        inv.setUpdatedAt(LocalDateTime.now());
                    });
            investmentRepository.saveAll(allocRequested);
            return false;
        }
    }

    // 투자금 송금 요청
    @Transactional(readOnly = false)
    public ApiResponseDto<Integer> requestWithdrawal(String projectId, String userSeq, String role) {
        ProductDetailDTO product = Optional.ofNullable(productClient.getProduct(projectId))
                .map(ResponseEntity::getBody)
                .orElseThrow(() -> new IllegalStateException("상품 정보를 가져올 수 없습니다."));

        if (!Objects.equals(product.getUserSeq(), userSeq)) {
            throw new IllegalStateException("상품 등록자만 출금을 요청할 수 있습니다.");
        }

        if (!"CREATOR".equals(role)) {
            throw new IllegalStateException("CREATOR만 출금을 요청할 수 있습니다.");
        }

        if (product.getEndDate() == null || LocalDate.now().isBefore(product.getEndDate())) {
            throw new IllegalStateException("아직 출금 가능한 시점이 아닙니다 (모집 종료 전).");
        }

        if (product.getPercent() == null || product.getPercent() < 80) {
            throw new IllegalStateException("달성률 미달 – 출금 불가.");
        }

        List<Investment> completed = investmentRepository.findByProjectId(projectId).stream()
                .filter(inv -> inv.getInvStatus() == InvestmentStatus.COMPLETED)
                .toList();
        int totalAmount = completed.stream().mapToInt(Investment::getInvestedPrice).sum();
        if (totalAmount <= 0) {
            throw new IllegalStateException("출금할 체결 투자금이 없습니다.");
        }

        MarketDto marketDto = MarketDto.builder()
                .investmentSeq(0) // TODO: 출금 요청을 따로 저장하지 않음
                .projectId(projectId)
                .userSeq(product.getUserSeq())
                .price(totalAmount)
                .build();

        try {
            ApiResponseDto<Integer> response = assetClient.requestWithdrawal(marketDto);
            log.info("[WITHDRAWAL] 송금 요청 성공 projectId={} amount={}", projectId, totalAmount);
            return response;
        } catch (Exception e) {
            log.error("[WITHDRAWAL] 송금 요청 실패 projectId={} reason={}", projectId, e.getMessage());
            throw new IllegalStateException("송금 요청 실패: " + e.getMessage());
        }
    }

    public VerifyInvestmentDto.Response verifyInvestments(VerifyInvestmentDto.Request requestDto) {
        try {
            List<Integer> investmentIdList = requestDto.getInvestments().stream()
                    .map(investment -> investment.getInvestmentId().intValue()).toList();

            Set<Integer> existedIdSet = investmentRepository.findByInvestmentSeqIn(investmentIdList).stream()
                    .map(Investment::getInvestmentSeq)
                    .collect(Collectors.toSet());

            VerifyInvestmentDto.Response response = VerifyInvestmentDto.Response.builder().result(new ArrayList<>())
                    .build();
            investmentIdList.forEach(investmentId -> {
                response.getResult().add(existedIdSet.contains(investmentId));
            });

            log.info("[Investment] 검증 결과 : {}", response.getResult());

            return response;
        } catch (Exception e) {
            log.error("[Investment] 체인링크 검증 중 오류 발생 : {}", e.getMessage());
            throw new RuntimeException("[Investment] 체인링크 검증 중 오류 발생 : " + e.getMessage());
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
