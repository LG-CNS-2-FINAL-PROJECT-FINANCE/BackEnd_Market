package com.ddiring.backend_market.investment.service;

import com.ddiring.backend_market.investment.dto.response.ListInvestmentResponse;
import com.ddiring.backend_market.investment.entity.Investment;
import com.ddiring.backend_market.investment.repository.ListInvestmentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ListInvestmentService {

    private final ListInvestmentRepository listInvestmentRepository;

    public List<ListInvestmentResponse> getListInvestment() {
        try {
            log.info("투자 상품 전체 조회 시작");
            List<Investment> investments = listInvestmentRepository.findAll();
            
            List<ListInvestmentResponse> response = investments.stream()
                    .map(investment -> ListInvestmentResponse.builder()
                            .productId(investment.getProductId())
                            .build())
                    .toList();
            
            log.info("투자 상품 전체 조회 완료: {}개 상품", response.size());
            return response;
        } catch (Exception e) {
            log.error("투자 상품 전체 조회 중 오류 발생", e);
            throw new RuntimeException("투자 상품 조회에 실패했습니다.", e);
        }
    }
}

