package com.ddiring.backend_market.investment.service;

import com.ddiring.backend_market.api.client.AssetClient;
import com.ddiring.backend_market.api.client.ProductClient;
import com.ddiring.backend_market.api.dto.AssetDTO;
import com.ddiring.backend_market.api.dto.ProductDTO;
import com.ddiring.backend_market.common.exception.BadParameter;
import com.ddiring.backend_market.investment.dto.request.BuyInvestmentRequest;
import com.ddiring.backend_market.investment.dto.response.ListInvestmentResponse;
import com.ddiring.backend_market.investment.entity.Investment;
import com.ddiring.backend_market.investment.repository.InvestmentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class InvestmentService {

    private final InvestmentRepository investmentRepository;
    private final ProductClient productClient;
    private final AssetClient assetClient;

    // 투자 상품 전체 조회
    public List<ListInvestmentResponse> getListInvestment() {

        log.info("투자 상품 전체 조회");

        List<Investment> investments = investmentRepository.findAll();

        return investments.stream()
                .map(investment -> {
                    ProductDTO product = productClient.getProduct(investment.getProductId());
                    return ListInvestmentResponse.builder()
                            .productId(investment.getProductId())
                            .title(product.getTitle())
                            .goalAmount(product.getGoalAmount())
                            .endDate(product.getEndDate())
                            .build();
                })
                .toList();
    }

    // 주문
    @Transactional
    public void buyInvestment(BuyInvestmentRequest request) {

        validateBuyRequest(request);

        ProductDTO product = productClient.getProduct(request.getProductId());

        if (!"승인 완료".equals(product.getStatus())) {
            throw new BadParameter("해당 상품은 현재 모집 중이 아닙니다.");
        }

        LocalDate now = LocalDate.now();
        
        Investment investment = Investment.builder()
                .userSeq(request.getUserSeq())
                .productId(request.getProductId())
                .tokenQuantity(request.getTokenQuantity())
                .investedPrice(request.getInvestedPrice())
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
                .investedPrice(request.getInvestedPrice())
                .build();

        assetClient.updateAsset(assetDTO);
    }

    // 유효성 검사
    private void validateBuyRequest(BuyInvestmentRequest request) {

        if (request == null) {
            throw new BadParameter("요청 데이터가 없습니다.");
        }

        if (request.getUserSeq() == null || request.getUserSeq() <= 0) {
            throw new BadParameter("유효하지 않은 사용자 ID입니다.");
        }

        if (request.getProductId() == null || request.getProductId() <= 0) {
            throw new BadParameter("유효하지 않은 상품 ID입니다.");
        }

        if (request.getTokenQuantity() == null || request.getTokenQuantity() <= 0) {
            throw new BadParameter("토큰 수량은 1개 이상이어야 합니다.");
        }

        if (request.getInvestedPrice() == null || request.getInvestedPrice() <= 0) {
            throw new BadParameter("투자 금액은 1원 이상이어야 합니다.");
        }
    }
}
