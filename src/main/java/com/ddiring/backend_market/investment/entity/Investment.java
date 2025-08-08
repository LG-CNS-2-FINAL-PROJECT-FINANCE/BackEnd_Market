package com.ddiring.backend_market.investment.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name = "investment")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Investment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "investment_seq", nullable = false)
    private Integer investmentSeq;

    @Column(name = "user_seq", nullable = false)
    private Integer userSeq;

    @Column(name = "project_id", nullable = false)
    private String projectId;

    @Column(name = "account")
    private String account;

    @Column(name = "invested_price", nullable = false)
    private Integer investedPrice;

    @Column(name = "token_quantity", nullable = false)
    private Integer tokenQuantity;

    @Column(name = "invested_at", nullable = false)
    private LocalDate investedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "inv_status", nullable = false)
    private InvestmentStatus invStatus;

    @Column(name = "current_amount", nullable = false)
    private Integer currentAmount;

    @Column(name = "total_investment", nullable = false)
    private Integer totalInvestment;

    @Column(name = "total_investor", nullable = false)
    private Integer totalInvestor;

    @Column(name = "achievement_rate", nullable = false)
    private Integer achievementRate;

    @Column(name = "created_id", nullable = false)
    private Integer createdId;

    @Column(name = "created_at", nullable = false)
    private LocalDate createdAt;

    @Column(name = "updated_id", nullable = false)
    private Integer updatedId;

    @Column(name = "updated_at", nullable = false)
    private LocalDate updatedAt;

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