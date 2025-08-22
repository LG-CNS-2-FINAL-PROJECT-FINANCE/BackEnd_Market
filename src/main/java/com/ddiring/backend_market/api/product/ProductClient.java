package com.ddiring.backend_market.api.product;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import java.util.List;

@FeignClient(name = "product", url = "http://localhost:8083")
public interface ProductClient {

    @GetMapping("/api/product")
    List<ProductDTO> getAllProduct();

    @GetMapping("/api/product/{productId}")
    ProductDTO getProduct(@PathVariable("productId") String productId);
}
