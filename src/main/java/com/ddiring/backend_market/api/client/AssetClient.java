package com.ddiring.backend_market.api.client;

import com.ddiring.backend_market.api.dto.AssetDTO;
import com.ddiring.backend_market.api.dto.ProductDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

// TODO: 실제 주소로 변경
@FeignClient(name = "asset-service", url = "${service.asset.url}")
public interface AssetClient {

    @PostMapping("api/asset/withdraw")
    void WithdrawAsset(@RequestBody AssetDTO dto);
}
