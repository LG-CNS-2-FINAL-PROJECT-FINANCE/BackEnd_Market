package com.ddiring.backend_market.investment.controller;

import com.ddiring.backend_market.api.client.ProductClient;
import com.ddiring.backend_market.api.dto.ProductDTO;
import com.ddiring.backend_market.investment.dto.request.BuyInvestmentRequest;
import com.ddiring.backend_market.investment.dto.request.CancelInvestmentRequest;
import com.ddiring.backend_market.investment.dto.response.*;
import com.ddiring.backend_market.investment.service.InvestmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

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
    public ResponseEntity<List<UserInvestmentListResponse>> getListByUserSeq(@PathVariable Integer userSeq) {

        return ResponseEntity.ok(investmentService.getUserInvestments(userSeq));
    }

    // 상품별 투자자 조회
    @GetMapping("/{productId}/userlist")
    public ResponseEntity<List<ProductInvestorListResponse>> getListInvestorByProductId(@PathVariable("productId") Integer projectId) {

        return ResponseEntity.ok(investmentService.getProductInvestor(projectId));
    }

    // 주문
    @PostMapping("/buy")
    public ResponseEntity<Map<String, Integer>> buyInvestment(
            @RequestBody BuyInvestmentRequest request,
            ProductDTO dto
    ) {

        investmentService.buyInvestment(request, dto);

        Map<String, Integer> response = Map.of("tokenQuantity", request.getTokenQuantity());
        return ResponseEntity.ok(response);
    }

    // 주문 취소
    @PostMapping("/cancel")
    public ResponseEntity<InvestmentResponse> cancelInvestment(
            @RequestBody CancelInvestmentRequest request
            ) {

        InvestmentResponse response = investmentService.cancelInvestment(request);
        return ResponseEntity.ok(response);
    }
}
