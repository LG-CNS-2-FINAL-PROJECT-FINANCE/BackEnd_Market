package com.ddiring.backend_market.api.asset;

import com.ddiring.backend_market.api.asset.dto.request.AssetDepositRequest;
import com.ddiring.backend_market.api.asset.dto.request.AssetEscrowRequest;
import com.ddiring.backend_market.api.asset.dto.request.AssetRefundRequest;
import com.ddiring.backend_market.api.asset.dto.request.AssetTokenRequest;
import com.ddiring.backend_market.api.asset.dto.response.AssetDepositResponse;
import com.ddiring.backend_market.api.asset.dto.response.AssetRefundResponse;
import com.ddiring.backend_market.common.dto.ApiResponseDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "assetClient", url = "${asset.base-url}")
public interface AssetClient {


    @GetMapping("/api/asset/wallet/search")
    ApiResponseDto<String> getWalletAddress(@RequestHeader("userSeq") String userSeq);

    @PostMapping("/api/asset/deposit")
    AssetDepositResponse requestDeposit(@RequestBody AssetDepositRequest request);

    @PostMapping("api/asset/token")
    void requestToken(@RequestBody AssetTokenRequest request);

    @PostMapping("/api/asset/refund")
    AssetRefundResponse requestRefund(@RequestBody AssetRefundRequest request);

    @PostMapping("/api/asset/escrow/release")
    ApiResponseDto<Void> releaseEscrowToSeller(@RequestBody AssetEscrowRequest request);

    // ✅ 거래 실패 시 구매자에게 예치금을 환불하는 API
    @PostMapping("/api/asset/escrow/refund")
    ApiResponseDto<Void> refundEscrowToBuyer(@RequestBody AssetEscrowRequest request);
}
