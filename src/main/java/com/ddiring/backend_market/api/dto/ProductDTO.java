package com.ddiring.backend_market.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductDTO {

    private String title; // 제목
    private Integer amount; // 모금액
    private Integer deadline; // 마감 기한
}
