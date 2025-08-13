package com.ddiring.backend_market.api.product;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductDTO {

    private String projectId; // 상품 번호
    private String title; // 제목
    private String content; // 본문
    private String summary; // 요약
    private Integer amount; // 모금액
    private Integer deadline; // 마감 기한
}
