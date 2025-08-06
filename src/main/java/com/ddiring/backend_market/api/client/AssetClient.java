package com.ddiring.backend_market.api.client;

import com.ddiring.backend_market.api.dto.AssetDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

// TODO: 실제 주소로 변경
@FeignClient(name = "asset-service", url = "${service.asset.url}")
public interface AssetClient {

    @PostMapping("/api/asset/update")
    void updateAsset(@RequestBody AssetDTO assetDTO);
}
