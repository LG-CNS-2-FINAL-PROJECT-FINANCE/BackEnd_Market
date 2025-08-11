package com.ddiring.backend_market.investment.service;

import com.ddiring.backend_market.api.asset.AssetClient;
import com.ddiring.backend_market.api.asset.dto.request.AssetDepositRequest;
import com.ddiring.backend_market.api.asset.dto.request.AssetRefundRequest;
import com.ddiring.backend_market.api.asset.dto.request.AssetTokenRequest;
import com.ddiring.backend_market.api.asset.dto.response.AssetDepositResponse;
import com.ddiring.backend_market.api.asset.dto.response.AssetRefundResponse;
import com.ddiring.backend_market.api.asset.dto.response.AssetTokenResponse;
import com.ddiring.backend_market.api.product.ProductClient;
import com.ddiring.backend_market.api.user.UserClient;
import com.ddiring.backend_market.api.product.ProductDTO;
import com.ddiring.backend_market.api.user.UserDTO;
import com.ddiring.backend_market.investment.dto.request.CancelInvestmentRequest;
import com.ddiring.backend_market.investment.dto.request.InvestmentRequest;
import com.ddiring.backend_market.investment.dto.response.*;
import com.ddiring.backend_market.investment.entity.Investment;
import com.ddiring.backend_market.investment.repository.InvestmentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class InvestmentService {

    private final InvestmentRepository investmentRepository;
    private final UserClient userClient;
    private final ProductClient productClient;
    private final AssetClient assetClient;

    // 투자 상품 전체 조회
    public List<ProductDTO> getAllProduct() {
        return productClient.getAllProduct();
    }

    // 개인 투자 내역 조회
    public List<MyInvestmentResponse> getMyInvestment(Integer userSeq) {
        List<Investment> myList = investmentRepository.findByUserSeq(userSeq);

        return myList.stream()
                .map(investment -> {
                    ProductDTO product = productClient.getProduct(investment.getProjectId());
                    return MyInvestmentResponse.builder()
                            .product(product)
                            .investedPrice(investment.getInvestedPrice())
                            .tokenQuantity(investment.getTokenQuantity())
                            .build();
                }).toList();
    }

    // 상품별 투자자 조회
    public List<ProductInvestorResponse> getInvestorByProduct(String projectId) {
        List<Investment> investments = investmentRepository.findByProjectId(projectId);

        List<Integer> investorList = investments.stream()
                .map(Investment::getUserSeq)
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
                .createdId(request.getUserSeq())
                .createdAt(LocalDateTime.now())
                .updatedId(request.getUserSeq())
                .updatedAt(LocalDateTime.now())
                .build();

        Investment saved = investmentRepository.save(investment);

        // Asset 투자금 예치 요청
        AssetDepositRequest depositRequest = new AssetDepositRequest();
        depositRequest.userSeq = request.getUserSeq();
        depositRequest.projectId = request.getProjectId();
        depositRequest.investedPrice = request.getInvestedPrice();

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

        // BC Connector 토큰 발행 요청
        AssetTokenRequest tokenRequest = new AssetTokenRequest();
        tokenRequest.userSeq = request.getUserSeq();
        tokenRequest.projectId = request.getProjectId();
        tokenRequest.investedPrice = request.getInvestedPrice();
        tokenRequest.tokenQuantity = request.getTokenQuantity();

        // 토큰 발행 실패 시
        AssetTokenResponse tokenResponse;
        try {
            tokenResponse = assetClient.requestToken(tokenRequest);
        } catch (Exception e1) {
            // 보상 트랜잭션
            AssetRefundRequest refundRequest = new AssetRefundRequest();
            refundRequest.userSeq = request.getUserSeq();
            refundRequest.projectId = request.getProjectId();
            refundRequest.investedPrice = request.getInvestedPrice();

            try {
                AssetRefundResponse refundResponse = assetClient.requestRefund(refundRequest);
            } catch (Exception e2) {
                // TODO: 예외 처리
            }

            saved.setInvStatus(Investment.InvestmentStatus.CANCELLED);
            saved.setUpdatedAt(LocalDateTime.now());
            investmentRepository.save(saved);

            return toResponse(saved);
        }

        // 토큰 발행 성공
        saved.setInvStatus(Investment.InvestmentStatus.COMPLETED);
        saved.setUpdatedAt(LocalDateTime.now());
        investmentRepository.save(saved);

        return toResponse(saved);
    }

    // 주문 취소
    public InvestmentResponse cancelInvestment(
            CancelInvestmentRequest request,
            Integer investmentSeq
            ) {
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
            // TODO: 토큰 회수 로직 협의
            AssetRefundRequest refundRequest = new AssetRefundRequest();
            refundRequest.userSeq = investment.getUserSeq();
            refundRequest.projectId = investment.getProjectId();
            refundRequest.investedPrice = investment.getInvestedPrice();

            try {
                AssetRefundResponse refundResponse = assetClient.requestRefund(refundRequest);
                if (refundResponse.success) {
                    investment.setInvStatus(Investment.InvestmentStatus.CANCELLED);
                    investment.setUpdatedAt(LocalDateTime.now());
                    investmentRepository.save(investment);

                    return toResponse(investment);
                } else {
                    // TODO: 보상 트랜잭션 + 모니터링 + 알람
                    // 환불 실패 시 상태 유지
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
