package com.ddiring.backend_market.api.product;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import java.util.List;

@FeignClient(name = "productClient", url = "http://localhost:8083")
public interface ProductClient {

    @GetMapping("/api/product/open")
    ResponseEntity<List<ProductDTO>> getAllProduct();

    @GetMapping("/api/product/unOpen")
    ResponseEntity<List<ProductDTO>> getAllUnOpenProduct();

    @GetMapping("/api/product/{projectId}")
    ResponseEntity<ProductDetailDTO> getProduct(@PathVariable("projectId") String projectId);
}
