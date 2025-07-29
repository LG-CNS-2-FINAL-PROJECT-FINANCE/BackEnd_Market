package com.ddiring.backend_market.investment.service;

import com.ddiring.backend_market.investment.dto.response.ListInvestmentResponse;
import com.ddiring.backend_market.investment.entity.Investment;
import com.ddiring.backend_market.investment.repository.ListInvestmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ListInvestmentService {

    private ListInvestmentRepository listInvestmentRepository;

    public List<ListInvestmentResponse> getAllInvestmentProducts() {
        List<Investment> investments = listInvestmentRepository.findAll();

        //TODO: Product Join
        return investments.stream()
                .map(
                        investment -> ListInvestmentResponse.builder()
                                .investmentSeq(investment.getInvestmentSeq())
                                .productId(investment.getProductId())
                                .title("TODO: Product Join") // TODO: Product Join
                                .goal_amount(10000) // TODO: Product Join
                                .startDate(LocalDate.now()) // TODO: Product Join
                                .endDate(LocalDate.now()) // TODO: Product Join
                                .build()
                )
                .toList();
    }
}
