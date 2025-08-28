package com.ddiring.backend_market.investment.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import java.time.LocalDateTime;

@Entity
@EntityListeners(AuditingEntityListener.class)
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

    @Column(name = "created_id")
    @CreatedBy
    private String createdId;

    @Setter
    @Column(name = "created_at")
    @CreatedDate
    private LocalDateTime createdAt;

    @Column(name = "updated_id")
    @LastModifiedBy
    private String updatedId;

    @Setter
    @Column(name = "updated_at")
    @LastModifiedDate
    private LocalDateTime updatedAt;

    @Getter
    public enum InvestmentStatus {
        PENDING("입금요청중"),
        FUNDING("예치완료_모집중"),
        ALLOC_REQUESTED("할당요청"),
        REJECTED("요청거절"),
        COMPLETED("체결"),
        FAILED("실패"),
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