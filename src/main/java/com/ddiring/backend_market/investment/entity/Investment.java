package com.ddiring.backend_market.investment.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import java.time.LocalDateTime;

@Entity
@Table(name = "investment")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class Investment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "investment_seq", nullable = false)
    private Integer investmentSeq;

    @Column(name = "user_seq", nullable = false)
    private Integer userSeq;

    @Column(name = "project_id", nullable = false)
    private String projectId;

    @Column(name = "invested_price", nullable = false)
    private Integer investedPrice;

    @Column(name = "token_quantity", nullable = false)
    private Integer tokenQuantity;

    @Column(name = "invested_at", nullable = false)
    private LocalDateTime investedAt;

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

    @CreatedDate
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_id", nullable = false)
    private Integer updatedId;

    @LastModifiedDate
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

        public boolean isPending() {return this == PENDING;}
        public boolean isCompleted() {return this == COMPLETED;}
        public boolean isCancelled() {return this == CANCELLED;}
    }
}