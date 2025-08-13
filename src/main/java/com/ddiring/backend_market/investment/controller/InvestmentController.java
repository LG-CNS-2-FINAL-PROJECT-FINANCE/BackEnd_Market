package com.ddiring.backend_market.investment.controller;

import com.ddiring.backend_market.api.product.ProductDTO;
import com.ddiring.backend_market.investment.dto.request.CancelInvestmentRequest;
import com.ddiring.backend_market.investment.dto.request.InvestmentRequest;
import com.ddiring.backend_market.investment.dto.response.*;
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
    public List<ProductDTO> getAllProduct() {
        return investmentService.getAllProduct();
    }

    // 개인 투자 내역 조회
    @GetMapping("/{userSeq}/list")
    public List<MyInvestmentResponse> getMyInvestment(@PathVariable("userSeq") Integer userSeq) {
        return investmentService.getMyInvestment(userSeq);
    }

    // 상품별 투자자 조회
    @GetMapping("/{projectId}/userlist")
    public ResponseEntity<List<ProductInvestorResponse>> getInvestorByProduct(@PathVariable String projectId) {
        return ResponseEntity.ok(investmentService.getInvestorByProduct(projectId));
    }

    // 주문
    @PostMapping("/buy")
    public ResponseEntity<InvestmentResponse> buyInvestment(@RequestBody InvestmentRequest request) {
        InvestmentResponse response = investmentService.buyInvestment(request);
        return ResponseEntity.ok(response);
    }

    // 주문 취소
    @PostMapping("/cancel")
    public ResponseEntity<InvestmentResponse> cancelInvestment(
            @RequestBody CancelInvestmentRequest request,
            @PathVariable Integer investmentSeq
    ) {
        InvestmentResponse response = investmentService.cancelInvestment(request, investmentSeq);
        return ResponseEntity.ok(response);
    }
}
