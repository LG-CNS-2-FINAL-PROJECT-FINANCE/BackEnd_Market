package com.ddiring.backend_market.investment.service;

import com.ddiring.backend_market.api.asset.AssetClient;
import com.ddiring.backend_market.api.asset.dto.request.AssetRequest;
import com.ddiring.backend_market.common.dto.ApiResponseDto;
import com.ddiring.backend_market.common.util.GatewayRequestHeaderUtils;
import com.ddiring.backend_market.api.product.ProductClient;
import com.ddiring.backend_market.api.user.UserClient;
import com.ddiring.backend_market.api.product.ProductDTO;
import com.ddiring.backend_market.api.user.UserDTO;
import com.ddiring.backend_market.event.dto.InvestRequestEvent;
import com.ddiring.backend_market.event.producer.InvestmentEventProducer;
import com.ddiring.backend_market.investment.dto.MarketDto;
import com.ddiring.backend_market.investment.dto.request.InvestmentRequest;
import com.ddiring.backend_market.investment.dto.response.*;
import com.ddiring.backend_market.investment.entity.Investment;
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
        Investment investment = Investment.builder()
                .userSeq(userSeq)
                .projectId(request.getProjectId())
                .investedPrice(request.getInvestedPrice())
                .tokenQuantity(request.getTokenQuantity())
                .investedAt(LocalDateTime.now())
                .invStatus(Investment.InvestmentStatus.PENDING)
                .build();

        Investment saved = investmentRepository.save(investment);

        // Asset 에스크로 예치 요청
        ProductDTO product = null;
        try {
            product = productClient.getProduct(request.getProjectId());
        } catch (Exception e) {
            log.warn("상품 조회 실패 projectId={} error={}", request.getProjectId(), e.getMessage());
        }

        AssetRequest assetRequest = AssetRequest.builder()
                .marketDto(MarketDto.builder()
                        .userSeq(GatewayRequestHeaderUtils.getUserSeq())
                        .price(request.getInvestedPrice())
                        .build())
                .productDto(product == null ? ProductDTO.builder()
                        .projectId(request.getProjectId())
                        .title(null)
                        .account(null)
                        .build() : product)
                .build();

        ApiResponseDto<Integer> depositResponse;
        try {
            depositResponse = assetClient.requestDeposit(assetRequest);
        } catch (Exception e) {
            saved.setInvStatus(Investment.InvestmentStatus.CANCELLED);
            saved.setUpdatedAt(LocalDateTime.now());
            investmentRepository.save(saved);

            return toResponse(saved);
        }
        boolean depositOk = depositResponse != null
                && "OK".equalsIgnoreCase(depositResponse.getCode());
        if (!depositOk) {
            saved.setInvStatus(Investment.InvestmentStatus.CANCELLED);
            saved.setUpdatedAt(LocalDateTime.now());
            investmentRepository.save(saved);
            return toResponse(saved);
        }

        // 입금 성공 => 펀딩 진행 상태로 변경
        saved.setInvStatus(Investment.InvestmentStatus.FUNDING);
        saved.setUpdatedAt(LocalDateTime.now());
        investmentRepository.save(saved);
        return toResponse(saved);
    }

    // 주문 취소
    @Transactional
    public InvestmentResponse cancelInvestment(String userSeq, String role, Integer investmentSeq) {
        Investment investment = investmentRepository.findById(investmentSeq)
                .orElseThrow(() -> new IllegalArgumentException("없는 주문입니다: " + investmentSeq));

        // 이미 취소된 경우 그대로 반환
        if (investment.isCancelled()) {
            return toResponse(investment);
        }

        // 1) PENDING -> 단순 취소
        if (investment.isPending()) {
            investment.setInvStatus(Investment.InvestmentStatus.CANCELLED);
            investment.setUpdatedAt(LocalDateTime.now());
            investmentRepository.save(investment); // 삭제 대신 상태만 변경하여 이력 유지
            return toResponse(investment);
        }

        // 2) FUNDING / ALLOC_REQUESTED / COMPLETED 단계 -> 환불 필요
        boolean requireRefund = switch (investment.getInvStatus()) {
            case FUNDING, ALLOC_REQUESTED, COMPLETED -> true;
            default -> false;
        };

        if (requireRefund) {
            ProductDTO product = null;
            try {
                product = productClient.getProduct(investment.getProjectId());
            } catch (Exception e) {
                log.warn("상품 조회 실패(환불) projectId={} error={}", investment.getProjectId(), e.getMessage());
            }

            AssetRequest refundReq = AssetRequest.builder()
                    .marketDto(MarketDto.builder()
                            .userSeq(GatewayRequestHeaderUtils.getUserSeq())
                            .price(investment.getInvestedPrice())
                            .build())
                    .productDto(product == null ? ProductDTO.builder()
                            .projectId(investment.getProjectId())
                            .title(null)
                            .build() : product)
                    .build();

            try {
                ApiResponseDto<Integer> refundResponse = assetClient.requestRefund(refundReq);
                boolean refundOk = refundResponse != null && "OK".equalsIgnoreCase(refundResponse.getCode());
                if (!refundOk) {
                    throw new IllegalStateException("환불 실패 (code!=OK)");
                }
            } catch (Exception e) {
                log.error("투자 환불 요청 실패 investmentSeq={} error={}", investmentSeq, e.getMessage());
                throw new IllegalStateException("환불 요청 실패");
            }
        }

        // 환불 성공 -> 상태 CANCELLED 후 삭제
        investment.setInvStatus(Investment.InvestmentStatus.CANCELLED);
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
                .filter(inv -> inv.getInvStatus() == Investment.InvestmentStatus.FUNDING)
                .collect(Collectors.toList());

        if (funding.isEmpty()) {
            log.info("FUNDING 투자 없음 projectId={}", projectId);
            return false;
        }

        // 상태 전환 ALLOC_REQUESTED
        funding.forEach(inv -> inv.setInvStatus(Investment.InvestmentStatus.ALLOC_REQUESTED));
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
        return true;
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
                .txHash(inv.getTxHash())
                .failureReason(inv.getFailureReason())
                .build();
    }
}
