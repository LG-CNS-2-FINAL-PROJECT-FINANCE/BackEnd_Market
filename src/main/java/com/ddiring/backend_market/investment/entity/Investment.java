package com.ddiring.backend_market.investment.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "investment")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Investment {

    // 주문 번호
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "investment_seq", nullable = false)
    private Integer investmentSeq;

    // 사용자 번호
    @Column(name = "user_seq", nullable = false)
    private String userSeq;

    // 프로젝트 번호
    @Column(name = "project_id", nullable = false)
    private String projectId;

    // 투자 금액
    @Column(name = "invested_price", nullable = false)
    private Integer investedPrice;

    // 토큰 수량
    @Column(name = "token_quantity", nullable = false)
    private Integer tokenQuantity;

    // 투자 일시
    @Column(name = "invested_at", nullable = false)
    private LocalDateTime investedAt;

    // 투자 상태
    @Setter
    @Enumerated(EnumType.STRING)
    @Column(name = "inv_status", nullable = false)
    private InvestmentStatus invStatus;

    // 현재 모금액
    @Column(name = "current_amount", nullable = false)
    private Integer currentAmount;

    //
    @Column(name = "total_investment", nullable = false)
    private Integer totalInvestment;

    // 총 투자자 수
    @Column(name = "total_investor", nullable = false)
    private Integer totalInvestor;

    // 달성률
    @Column(name = "achievement_rate", nullable = false)
    private Integer achievementRate;

    @Column(name = "created_id", nullable = false)
    private Integer createdId;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_id", nullable = false)
    private Integer updatedId;

    @Setter
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Getter
    public enum InvestmentStatus {
        PENDING("대기"),
        COMPLETED("체결"),
        CANCELLED("취소");

        private final String description;

        InvestmentStatus(String description) {
            this.description = description;
        }

    }

    public boolean isPending() {
        return this.invStatus == InvestmentStatus.PENDING;
    }

    public boolean isCompleted() {
        return this.invStatus == InvestmentStatus.COMPLETED;
    }

    public boolean isCancelled() {
        return this.invStatus == InvestmentStatus.CANCELLED;
    }
}