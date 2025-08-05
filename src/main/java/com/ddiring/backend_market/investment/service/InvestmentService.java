package com.ddiring.backend_market.investment.service;

import com.ddiring.backend_market.api.client.AssetClient;
import com.ddiring.backend_market.api.client.ProductClient;
import com.ddiring.backend_market.api.dto.AssetDTO;
import com.ddiring.backend_market.api.dto.ProductDTO;
import com.ddiring.backend_market.common.exception.BadParameter;
import com.ddiring.backend_market.investment.dto.request.BuyInvestmentRequest;
import com.ddiring.backend_market.investment.dto.request.CancelInvestmentRequest;
import com.ddiring.backend_market.investment.dto.response.*;
import com.ddiring.backend_market.investment.entity.Investment;
import com.ddiring.backend_market.investment.repository.InvestmentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class InvestmentService {

    private final InvestmentRepository investmentRepository;
    private final ProductClient productClient;
    private final AssetClient assetClient;

    // 투자 상품 전체 조회
    public List<AllProductListResponse> getAllProducts() {

        List<Investment> investments = investmentRepository.findAll();

        return investments.stream()
                .map(investment -> {
                    ProductDTO product = productClient.getProduct(investment.getProjectId());
                    return AllProductListResponse.builder()
                            .projectId(investment.getProjectId())
                            .title(product.getTitle())
                            .currentAmount(investment.getCurrentAmount())
                            .achievementRate(investment.getAchievementRate())
                            .startDate(product.getStartDate())
                            .endDate(product.getEndDate())
                            .build();
                })
                .toList();
    }

    // 개인 투자 내역 조회
    public List<UserInvestmentListResponse> getUserInvestments(Integer userSeq) {

        return investmentRepository.findByUserSeq(userSeq).stream()
                .map(investment -> {
                    ProductDTO product = productClient.getProduct(investment.getProjectId());
                    return UserInvestmentListResponse.builder()
                            .userSeq(investment.getUserSeq())
                            .projectId(investment.getProjectId())
                            .title(product.getTitle())
                            .investedPrice(investment.getInvestedPrice())
                            .tokenQuantity(investment.getTokenQuantity())
                            .build();
                }).collect(Collectors.toList());
    }

    // 상품별 투자자 조회
    public List<ProductInvestorListResponse> getProductInvestor(Integer projectId) {

        return investmentRepository.findByProjectId(projectId).stream()
                .map(investment -> {
                    ProductDTO product = productClient.getProduct(investment.getProjectId());
                    return ProductInvestorListResponse.builder()
                            .projectId(investment.getProjectId())
                            .totalInvestment(investment.getTotalInvestment())
                            .totalInvestors(investment.getTotalInvestor())
                            .build();
                }).collect(Collectors.toList());
    }


    // 주문
    @Transactional
    public void buyInvestment(BuyInvestmentRequest request, ProductDTO dto) {

        Investment order = investmentRepository.findById(request.getInvestmentSeq())
                .orElseThrow(() -> new BadParameter("존재하지 않는 주문입니다."));

        // 기본 값 유효성 검사
        validateBuyRequest(request, dto);

        ProductDTO product = productClient.getProduct(request.getProductId());

        if (!"승인 완료".equals(product.getStatus())) {
            throw new BadParameter("해당 상품은 현재 모집 중이 아닙니다.");
        }

        // 투자 금액 계산 (토큰 1개의 가격 * 토큰수량)
        Integer investedPrice = dto.getMinInvestment() * request.getTokenQuantity();

        LocalDate now = LocalDate.now();
        Investment investment = Investment.builder()
                .userSeq(request.getUserSeq())
                .projectId(request.getProductId())
                .tokenQuantity(request.getTokenQuantity())
                .investedPrice(investedPrice)
                .investedAt(now)
                .createdId(request.getUserSeq())
                .createdAt(now)
                .updatedId(request.getUserSeq())
                .updatedAt(now)
                .build();

        investmentRepository.save(investment);

        AssetDTO assetDTO = AssetDTO.builder()
                .userSeq(request.getUserSeq())
                .productId(request.getProductId())
                .tokenQuantity(request.getTokenQuantity())
                .investedPrice(investedPrice)
                .build();

        assetClient.updateAsset(assetDTO);

        InvestmentResponse.builder()
                .tokenQuantity(order.getTokenQuantity())
                .build();
    }

    // 주문 취소
    public InvestmentResponse cancelInvestment(CancelInvestmentRequest request) {

        Investment order = investmentRepository.findById(request.getInvestmentSeq())
                .orElseThrow(() -> new BadParameter("존재하지 않는 주문입니다."));

        if (!order.getUserSeq().equals(request.getUserSeq()) || !order.getProjectId().equals(request.getProductId())) {
            throw new BadParameter("주문 정보가 일치하지 않습니다.");
        }

        if (order.isCancelled()) {
            throw new BadParameter("이미 취소된 주문입니다.");
        }

        order.isCancelled();

        // TODO: 환불 처리

        investmentRepository.save(order);

        return InvestmentResponse.builder()
                .tokenQuantity(order.getTokenQuantity())
                .build();
    }

    // 유효성 검사
    private void validateBuyRequest(BuyInvestmentRequest request, ProductDTO dto) {

        if (request == null) {
            throw new BadParameter("요청 데이터가 없습니다.");
        }

        if (request.getUserSeq() == null || request.getUserSeq() <= 0) {
            throw new BadParameter("유효하지 않은 사용자입니다.");
        }

        if (request.getProductId() == null || request.getProductId() <= 0) {
            throw new BadParameter("유효하지 않은 상품입니다.");
        }

        if (request.getTokenQuantity() == null || request.getTokenQuantity() <= 0) {
            throw new BadParameter("토큰 수량은 1개 이상이어야 합니다.");
        }

        if (dto.getMinInvestment() == null || dto.getMinInvestment() <= 0) {
            throw new BadParameter("최소 투자 금액이 설정되지 않았습니다.");
        }
    }
}
