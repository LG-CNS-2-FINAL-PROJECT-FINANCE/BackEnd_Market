package com.ddiring.backend_market.api.asset;

import com.ddiring.backend_market.api.asset.dto.request.AssetDepositRequest;
import com.ddiring.backend_market.api.asset.dto.request.AssetRefundRequest;
import com.ddiring.backend_market.api.asset.dto.request.AssetTokenRequest;
import com.ddiring.backend_market.api.asset.dto.response.AssetDepositResponse;
import com.ddiring.backend_market.api.asset.dto.response.AssetRefundResponse;
import com.ddiring.backend_market.api.asset.dto.response.AssetTokenResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "asset", url = "http://localhost:8082")
public interface AssetClient {

    @PostMapping("/api/asset/deposit")
    AssetDepositResponse requestDeposit(@RequestBody AssetDepositRequest request);

    @PostMapping("api/asset/token")
    AssetTokenResponse requestToken(@RequestBody AssetTokenRequest request);

    @PostMapping("/api/asset/refund")
    AssetRefundResponse requestRefund(@RequestBody AssetRefundRequest request);
}
