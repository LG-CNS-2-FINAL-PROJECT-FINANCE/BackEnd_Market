package com.ddiring.backend_market.investment.controller;

import com.ddiring.backend_market.investment.dto.request.BuyInvestmentRequest;
import com.ddiring.backend_market.investment.dto.response.ListInvestmentResponse;
import com.ddiring.backend_market.investment.service.InvestmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/market/invest")
@RequiredArgsConstructor
public class InvestmentController {

    private final InvestmentService investmentService;

    // 투자 상품 전체 조회
    @GetMapping("/list")
    public ResponseEntity<List<ListInvestmentResponse>> getListInvestment() {

        return ResponseEntity.ok(investmentService.getListInvestment());
    }

    // 주문
    @PostMapping("/buy")
    public ResponseEntity<Void> buyInvestment(
            @RequestBody BuyInvestmentRequest request
    ) {

        investmentService.buyInvestment(request);
        return ResponseEntity.ok().build();
    }
}
