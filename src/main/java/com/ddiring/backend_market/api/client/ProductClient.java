package com.ddiring.backend_market.api.client;

import com.ddiring.backend_market.api.dto.ProductDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

// TODO: 병합시 실제 주소로 변경
@FeignClient(name = "product")
public interface ProductClient {

    @GetMapping("/product/{projectId}")
    ProductDTO getProduct(@PathVariable String projectId);

    @PostMapping("/product/list")
    List<ProductDTO> getProductByProjectId(@RequestBody List<String> projectId);
}
