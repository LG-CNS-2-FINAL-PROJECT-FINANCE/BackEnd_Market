package com.ddiring.backend_market.investment.controller;

import com.ddiring.backend_market.api.product.ProductDTO;
import com.ddiring.backend_market.common.dto.ApiResponseDto;
import com.ddiring.backend_market.investment.dto.VerifyInvestmentDto;
import com.ddiring.backend_market.investment.dto.request.CancelInvestmentRequest;
import com.ddiring.backend_market.investment.dto.request.InvestmentRequest;
import com.ddiring.backend_market.investment.dto.response.*;
import com.ddiring.backend_market.investment.scheduler.AllocationScheduler;
import com.ddiring.backend_market.investment.service.InvestmentService;
import lombok.RequiredArgsConstructor;
import com.ddiring.backend_market.common.util.GatewayRequestHeaderUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Slf4j
@RestController
@RequestMapping("/api/market/invest")
@RequiredArgsConstructor
public class InvestmentController {

    private final InvestmentService investmentService;

    // 투자 상품 전체 조회
    @GetMapping("/list")
    public List<ProductDTO> getAllProduct() {
        return investmentService.getAllProduct();
    }

    // 개인 투자 내역 조회
    @GetMapping("/mylist")
    public ResponseEntity<List<MyInvestmentResponse>> getMyInvestment() {
        String userSeq = GatewayRequestHeaderUtils.getUserSeq();
        return ResponseEntity.ok(investmentService.getMyInvestment(userSeq));
    }

    // 상품 주문 확인
    @GetMapping("/{projectId}/mylist")
    public ResponseEntity<List<MyInvestmentByProductResponse>> getMyInvestmentByProduct(
            @PathVariable String projectId) {
        String userSeq = GatewayRequestHeaderUtils.getUserSeq();
        return ResponseEntity.ok(investmentService.getMyInvestmentByProduct(userSeq, projectId));
    }

    // 상품별 투자자 조회
    @GetMapping("/{projectId}/userlist")
    public ResponseEntity<List<ProductInvestorResponse>> getInvestorByProduct(@PathVariable String projectId) {
        return ResponseEntity.ok(investmentService.getInvestorByProduct(projectId));
    }

    // 주문
    @PostMapping("/{projectId}/buy")
    public ResponseEntity<InvestmentResponse> buyInvestment(@PathVariable String projectId,
            @RequestBody InvestmentRequest request) {
        String userSeq = GatewayRequestHeaderUtils.getUserSeq();
        String role = GatewayRequestHeaderUtils.getRole();
        InvestmentResponse response = investmentService.buyInvestment(projectId, userSeq, role, request);
        return ResponseEntity.ok(response);
    }

    // 주문 취소
    @PostMapping("/{investmentSeq}/cancel")
    public ResponseEntity<InvestmentResponse> cancelInvestment(@PathVariable Integer investmentSeq,
            @RequestBody CancelInvestmentRequest request) {
        String userSeq = GatewayRequestHeaderUtils.getUserSeq();
        String role = GatewayRequestHeaderUtils.getRole();
        InvestmentResponse response = investmentService.cancelInvestment(userSeq, role, request);
        return ResponseEntity.ok(response);
    }

    // 조건 충족 시 토큰 할당(온체인 요청) 호출
    @PostMapping("/{projectId}/allocate")
    public ResponseEntity<String> triggerAllocation(@PathVariable String projectId) {
        boolean sent = investmentService.triggerAllocationIfEligible(projectId);
        return ResponseEntity.ok(sent ? "REQUEST_SENT" : "NOT_ELIGIBLE");
    }

    // (CREATOR) 출금 요청
    @PostMapping("/{projectId}/withdrawal")
    public ApiResponseDto<Integer> requestWithdrawal(@PathVariable String projectId) {
        String userSeq = GatewayRequestHeaderUtils.getUserSeq();
        String role = GatewayRequestHeaderUtils.getRole();
        return investmentService.requestWithdrawal(projectId, userSeq, role);
    }

    @PostMapping("/verify")
    public ApiResponseDto<?> verifyInvestmentChainlink(@RequestBody VerifyInvestmentDto.Request requestDto) {
        VerifyInvestmentDto.Response response = investmentService.verifyInvestments(requestDto);
        return ApiResponseDto.createOk(response);
    }

    private final AllocationScheduler scheduler;
    @PostMapping("/test/allocation")
    public ApiResponseDto<?> testAllocateInvestToken() {
        scheduler.dailyAllocationCheck();
        return ApiResponseDto.defaultOk();
    }
}
