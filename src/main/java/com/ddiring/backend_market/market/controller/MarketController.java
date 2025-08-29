package com.ddiring.backend_market.market.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ddiring.backend_market.market.dto.request.ProfitRequest;
import com.ddiring.backend_market.market.service.MarketService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/market/")
@RequiredArgsConstructor
public class MarketController {

    private final MarketService marketService;

    // 수익 분배 요청
    @PostMapping("/profit")
    public ResponseEntity<String> profit(@RequestBody ProfitRequest profitRequest) {
        marketService.distributeProfit(profitRequest);
        return ResponseEntity.ok("SUCCESS");
    }
}
