package com.ddiring.backend_market.api.product;

import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductDTO {

    private String projectId; // 상품 번호
    private String userSeq; // 사용자 번호 (등록자)
    private String title; // 제목
    private String content; // 본문
    private String summary; // 요약
    private Integer minInvestment; // 최소 투자 금액
    private String account; // 모집 계좌
    private Integer goalAmount; // 목표금액
    private Integer amount; // 모금액
    private LocalDate endDate; // 종료일
    private Integer deadline; // 마감 기한
    private Integer percent; // 달성률
}
