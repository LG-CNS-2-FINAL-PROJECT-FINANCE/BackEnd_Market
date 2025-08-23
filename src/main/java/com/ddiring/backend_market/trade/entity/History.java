package com.ddiring.backend_market.trade.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "history")
public class History {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "history_seq", nullable = false)
    private Integer historySeq;

    @Column(name = "project_id", nullable = false)
    private String projectId;

    @Column(name = "user_Seq", nullable = false)
    private String userSeq;

    @Column(name = "trade_type", nullable = false)
    private Integer tradeType;

    @Column(name = "trade_price", nullable = false)
    private Integer tradePrice;

    @Column(name = "token_quantity", nullable = false)
    private Integer tokenQuantity;

    @Column(name = "traded_at", nullable = false)
    private LocalDateTime tradedAt;

    @Column(name = "created_id")
    private Integer createdId;

    @Column(name = "created_at")
    private LocalDate createdAt;

    @Column(name = "updated_id")
    private Integer updatedId;

    @Column(name = "updated_at")
    private LocalDate updatedAt;

    @Builder
    public History(String projectId, String userSeq, Integer tradeType, Integer tradePrice, Integer tokenQuantity, LocalDateTime tradedAt, Integer createdId, LocalDate createdAt, Integer updatedId, LocalDate updatedAt) {
        this.projectId = projectId;
        this.userSeq = userSeq;
        this.tradeType = tradeType;
        this.tradePrice = tradePrice;
        this.tokenQuantity = tokenQuantity;
        this.tradedAt = tradedAt;
        this.createdId = createdId;
        this.createdAt = createdAt;
        this.updatedId = updatedId;
        this.updatedAt = updatedAt;
    }

}
