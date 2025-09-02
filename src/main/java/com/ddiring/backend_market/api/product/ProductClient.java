package com.ddiring.backend_market.api.product;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.ddiring.backend_market.common.dto.ApiResponseDto;

import java.util.List;

@FeignClient(name = "productClient", url = "${product.base-url}")
public interface ProductClient {

    @GetMapping("/api/product")
    ApiResponseDto<List<ProductDTO>> getAllProduct();

    @GetMapping("/api/product/{projectId}")
    ApiResponseDto<ProductDTO> getProduct(@PathVariable("projectId") String projectId);
}
