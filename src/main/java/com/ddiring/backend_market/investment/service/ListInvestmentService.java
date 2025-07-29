package com.ddiring.backend_market.investment.service;

import com.ddiring.backend_market.investment.dto.response.ListInvestmentResponse;
import com.ddiring.backend_market.investment.entity.Investment;
import com.ddiring.backend_market.investment.repository.ListInvestmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ListInvestmentService {

    private ListInvestmentRepository listInvestmentRepository;

    public List<ListInvestmentResponse> getListInvestment() {
        List<Investment> investments = listInvestmentRepository.findAll();

        return investments.stream()
                .map(
                        investment -> ListInvestmentResponse.builder()
                                .productId(investment.getProductId())
                                .build()
                )
                .toList();
    }
}

