package com.ddiring.backend_market.investment.service;

import com.ddiring.backend_market.api.asset.AssetClient;
import com.ddiring.backend_market.api.asset.dto.request.AssetDepositRequest;
import com.ddiring.backend_market.api.asset.dto.request.AssetRefundRequest;
import com.ddiring.backend_market.api.asset.dto.response.AssetDepositResponse;
import com.ddiring.backend_market.api.asset.dto.response.AssetRefundResponse;
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
    public InvestmentResponse buyInvestment(InvestmentRequest request) {
        Investment investment = Investment.builder()
                .userSeq(request.getUserSeq())
                .projectId(request.getProjectId())
                .investedPrice(request.getInvestedPrice())
                .tokenQuantity(request.getTokenQuantity())
                .investedAt(LocalDateTime.now())
                .invStatus(Investment.InvestmentStatus.PENDING)
                .build();

        Investment saved = investmentRepository.save(investment);

        // Asset 투자금 예치 요청
        AssetDepositRequest depositRequest = new AssetDepositRequest();
        depositRequest.userSeq = request.getUserSeq();
        depositRequest.projectId = request.getProjectId();
        depositRequest.price = request.getInvestedPrice();

        AssetDepositResponse depositResponse;
        try {
            depositResponse = assetClient.requestDeposit(depositRequest);
        } catch (Exception e) {
            saved.setInvStatus(Investment.InvestmentStatus.CANCELLED);
            saved.setUpdatedAt(LocalDateTime.now());
            investmentRepository.save(saved);

            return toResponse(saved);
        }

        if (!depositResponse.success) {
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
    public InvestmentResponse cancelInvestment(
            CancelInvestmentRequest request,
            Integer investmentSeq) {
        Optional<Investment> opt = investmentRepository.findById(investmentSeq);
        if (opt.isEmpty()) {
            throw new IllegalArgumentException("없는 주문입니다: " + investmentSeq);
        }

        Investment investment = opt.get();

        if (investment.isPending()) {
            investment.setInvStatus(Investment.InvestmentStatus.CANCELLED);
            investment.setUpdatedAt(LocalDateTime.now());
            investmentRepository.save(investment);

            return toResponse(investment);
        } else if (investment.isCompleted()) {
            // TODO: 토큰 회수 로직 협의 (DB 삭제 ?)
            AssetRefundRequest refundRequest = new AssetRefundRequest();
            refundRequest.userSeq = investment.getUserSeq();
            refundRequest.projectId = investment.getProjectId();
            refundRequest.price = investment.getInvestedPrice();

            try {
                AssetRefundResponse refundResponse = assetClient.requestRefund(refundRequest);
                if (refundResponse.success) {
                    investment.setInvStatus(Investment.InvestmentStatus.CANCELLED);
                    investment.setUpdatedAt(LocalDateTime.now());
                    investmentRepository.save(investment);

                    return toResponse(investment);
                } else {
                    // TODO: 보상 트랜잭션 + 모니터링 + 알람
                    // 환불 실패 시 상태 유지 - 협의 필요
                    throw new IllegalStateException("환불 실패");
                }
            } catch (Exception e) {
                throw new IllegalStateException("환불 요청 실패");
            }
        } else {
            // 취소 완료
            return toResponse(investment);
        }
    }

    // 투자 할당 요청 트리거
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
