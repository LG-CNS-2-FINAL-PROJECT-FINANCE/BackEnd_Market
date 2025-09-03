package com.ddiring.backend_market.api.asset;

import com.ddiring.backend_market.api.asset.dto.request.*;
import com.ddiring.backend_market.common.dto.ApiResponseDto;
import com.ddiring.backend_market.api.asset.dto.request.MarketDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "assetClient", url = "${asset.base-url}")
public interface AssetClient {

    @GetMapping("/api/asset/wallet/search")
    ApiResponseDto<String> getWalletAddress(@RequestHeader("userSeq") String userSeq);

    @PostMapping("/api/asset/escrow/withdrawal")
    ApiResponseDto<Integer> requestWithdrawal(@RequestBody MarketDto marketDto);

    @PostMapping("/api/asset/escrow/refund")
    ApiResponseDto<Void> refundEscrowToBuyer(@RequestBody AssetEscrowRequest request);

    @PostMapping("/api/asset/market/buy")
    ApiResponseDto<String> marketBuy(@RequestHeader("userSeq") String userSeq, @RequestHeader("role") String role,
            @RequestBody MarketBuyDto marketBuyDto);

    @PostMapping("/api/asset/market/sell")
    ApiResponseDto<String> marketSell(@RequestHeader("userSeq") String userSeq,
            @RequestBody MarketSellDto marketSellDto);

    @PostMapping("/api/asset/market/refund")
    ApiResponseDto<String> marketRefund(@RequestHeader("userSeq") String userSeq, @RequestHeader("role") String role,
            @RequestBody MarketRefundDto marketRefundDto);

    @PostMapping("/api/asset/title")
    String getMarketTitle(@RequestBody TitleRequestDto titleRequestDto);

    @PostMapping("/api/asset/get/token/{projectId}")
    void getToken(@PathVariable String projectId, String userSeq, @RequestBody MarketTokenDto marketTokenDto);
}
