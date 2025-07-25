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
    private Integer projectId;

    @Column(name = "invested_price", nullable = false)
    private Integer investedPrice;

    @Column(name = "token_quantity", nullable = false)
    private Integer tokenQuantity;

    @Column(name = "invested_at", nullable = false)
    private LocalDate investedAt;

    @Column(name = "created_id", nullable = false)
    private Integer createdId;

    @Column(name = "created_at", nullable = false)
    private LocalDate createdAt;

    @Column(name = "updated_id", nullable = false)
    private Integer updatedId;

    @Column(name = "updated_at", nullable = false)
    private LocalDate updatedAt;
}