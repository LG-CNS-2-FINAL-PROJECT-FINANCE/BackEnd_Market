package com.ddiring.backend_market.api.product;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

// TODO: 병합 시 주소 맞춰야함
@FeignClient(name = "product")
public interface ProductClient {

    @GetMapping("/api/product")
    List<ProductDTO> getAllProduct();

    @GetMapping("/api/product/{productId}")
    ProductDTO getProduct(@PathVariable("productID") String productId);
}
