package com.ddiring.backend_market.api.product;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "productClient", url = "${product.base-url}")
public interface ProductClient {

    @GetMapping("/api/product")
    List<ProductDTO> getAllProduct();

    @GetMapping("/api/product/batch")
    List<ProductDTO> getProducts(@RequestParam("projectId") List<String> projectId);
}
