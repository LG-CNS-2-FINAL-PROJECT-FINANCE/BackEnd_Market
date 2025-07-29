package com.ddiring.backend_market.investment.service;

import com.ddiring.backend_market.investment.dto.response.DetailedInvestmentResponse;
import com.ddiring.backend_market.investment.entity.Investment;
import com.ddiring.backend_market.investment.repository.DetailedInvestmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DetailedInvestmentService {

    private DetailedInvestmentRepository detailedInvestmentRepository;

    public List<DetailedInvestmentResponse> getDetailedInvestment() {
        List<Investment> investments = detailedInvestmentRepository.findAll();

        // TODO: Product Join
        return investments.stream()
                .map(
                        investment -> DetailedInvestmentResponse.builder()
                                .productId(investment.getProductId())
                                .title("TODO: Product Join") // TODO: Product Join
                                .content("TODO: Product Join") // TODO: Product Join
                                .goal_amount(10000) // TODO: Product Join
                                .min_investment(100) // TODO: Product Join
                                .account(1234) // TODO: Product Join
                                .build()
                ).toList();
    }
}
