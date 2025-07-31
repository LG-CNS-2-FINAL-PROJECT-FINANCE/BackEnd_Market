package com.ddiring.backend_market.investment.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import java.time.LocalDate;
import java.util.List;

@Slf4j
@Entity
@Table(name = "product")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_id", nullable = false)
    private Integer productId;

    @Column(name = "user_seq", nullable = false)
    private Integer userSeq;

    @Column(name = "status", nullable = false)
    private Integer status;  // -1: 숨김, 0: 등록대기, 1: 승인완료

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(name = "title", nullable = false, length = 255)
    private String title;

    @Column(name = "summary", nullable = false, columnDefinition = "TEXT")
    private String summary;

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "goal_amount", nullable = false)
    private Integer goalAmount;

    @Column(name = "min_investment", nullable = false)
    private Integer minInvestment;

    @Column(name = "account", nullable = false)
    private Integer account;

    @Column(name = "document", nullable = false, columnDefinition = "TEXT")
    private String document;  // JSON 형태로 저장 (보고서, 이미지, 중단 사유서)

    @Column(name = "created_id", nullable = false)
    private Integer createdId;

    @Column(name = "created_at", nullable = false)
    private LocalDate createdAt;

    @Column(name = "updated_id", nullable = false)
    private Integer updatedId;

    @Column(name = "updated_at", nullable = false)
    private LocalDate updatedAt;
} 