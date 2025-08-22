package com.ddiring.backend_market.investment.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "investment")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Investment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "investment_seq")
    private Integer investmentSeq;

    @Column(name = "user_seq")
    private String userSeq;

    @Column(name = "project_id")
    private String projectId;

    @Column(name = "account")
    private String account;

    @Column(name = "invested_price")
    private Integer investedPrice;

    @Column(name = "token_quantity")
    private Integer tokenQuantity;

    @Column(name = "invested_at")
    private LocalDateTime investedAt;

    @Setter
    @Enumerated(EnumType.STRING)
    @Column(name = "inv_status")
    private InvestmentStatus invStatus;

    @Column(name = "current_amount")
    private Integer currentAmount;

    @Column(name = "total_investment")
    private Integer totalInvestment;

    @Column(name = "total_investor")
    private Integer totalInvestor;

    @Column(name = "achievement_rate")
    private Integer achievementRate;

    @Column(name = "created_id")
    private Integer createdId;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_id")
    private Integer updatedId;

    @Setter
    @Column(name = "updated_at")
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