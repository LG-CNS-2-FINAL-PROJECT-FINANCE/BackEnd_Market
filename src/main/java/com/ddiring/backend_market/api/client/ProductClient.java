package com.ddiring.backend_market.api.client;

import com.ddiring.backend_market.api.dto.ProductDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

// TODO: 실제 주소로 변경
@FeignClient(name = "productClient", url = "http://localhost:8081")
public interface ProductClient {

    @GetMapping("/api/product/{productId}/market")
    ProductDTO getListInvestment(@PathVariable("productId") Integer productId);
}
