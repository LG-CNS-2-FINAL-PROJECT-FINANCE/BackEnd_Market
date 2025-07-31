package com.ddiring.backend_market.investment.service;

import com.ddiring.backend_market.api.client.ProductClient;
import com.ddiring.backend_market.api.dto.ProductDTO;
import com.ddiring.backend_market.investment.dto.response.ListInvestmentResponse;
import com.ddiring.backend_market.investment.entity.Investment;
import com.ddiring.backend_market.investment.repository.InvestmentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class InvestmentService {

    private final InvestmentRepository investmentRepository;
    private final ProductClient productClient;

    // 투자 상품 전체 조회
    public List<ListInvestmentResponse> getListInvestment() {

        log.info("투자 상품 전체 조회");

        List<Investment> investments = investmentRepository.findAll();

        return investments.stream()
                .map(investment -> {
                    ProductDTO product = productClient.getListInvestment(investment.getProductId());
                    return ListInvestmentResponse.builder()
                            .productId(investment.getProductId())
                            .title(product.getTitle())
                            .goalAmount(product.getGoalAmount())
                            .endDate(product.getEndDate())
                            .build();
                })
                .toList();
    }

//    @ResponseStatus(HttpStatus.BAD_REQUEST)
//    @ExceptionHandler({ClientError.class})
//    public int buyInvestment(Integer productId, Integer investedPrice, Integer tokenQuantity, BuyException e) {
//
//        Investment buyInvestment =
//    }
}
