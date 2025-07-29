package com.ddiring.backend_market.investment.controller;

import com.ddiring.backend_market.investment.dto.response.ListInvestmentResponse;
import com.ddiring.backend_market.investment.service.ListInvestmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping(value = "/market/invest", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class InvestmentController {

    private final ListInvestmentService listInvestmentService;

    // 투자 상품 전체 조회
    @GetMapping("/list")
    public ResponseEntity<List<ListInvestmentResponse>> getListInvestment() {
        return ResponseEntity.ok(listInvestmentService.getAllInvestmentProducts());
    }
}
