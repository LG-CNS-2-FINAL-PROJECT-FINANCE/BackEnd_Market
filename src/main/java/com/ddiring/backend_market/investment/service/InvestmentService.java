package com.ddiring.backend_market.investment.service;

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

    public List<ListInvestmentResponse> getListInvestment() {
        try {
            log.info("투자 상품 전체 조회");

            List<Investment> investments = investmentRepository.findAll();

            List<ListInvestmentResponse> response = investments.stream()
                    .map(investment -> ListInvestmentResponse.builder()
                            .productId(investment.getProductId())
                            .build())
                    .toList();

            log.info("투자 상품 전체 조회 완료: {}개", response.size());
            return response;
        } catch (Exception e) {
            log.error("투자 상품 전체 조회 실패", e);
            throw new RuntimeException(e);
        }
    }
}
