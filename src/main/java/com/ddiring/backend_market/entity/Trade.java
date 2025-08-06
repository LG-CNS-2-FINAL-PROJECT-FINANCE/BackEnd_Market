package com.ddiring.backend_market.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "trade")
public class Trade {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "trade_seq", nullable = false)
    private Integer tradeSeq;           // 체결 번호 (PK, auto)

    @Column(name = "project_id", nullable = false)
    private Integer projectId;           // 프로젝트 번호 (FK)

    @Column(name = "purchase_id", nullable = false)
    private Integer purchaseId;          // 구매 주문 번호 (FK)

    @Column(name = "sell_id", nullable = false)
    private Integer sellId;              // 판매 주문 번호 (FK)

    @Column(name = "trade_price", nullable = false)
    private Integer tradePrice;          // 체결 금액

    @Column(name = "token_quantity", nullable = false)
    private Integer tokenQuantity;       // 토큰 수량

    @Column(name = "traded_at", nullable = false)
    private LocalDate tradedAt;          // 체결일자

    @Column(name = "created_id")
    private Integer createdId;           // 생성자

    @Column(name = "created_at")
    private LocalDate createdAt;         // 생성일자

    @Column(name = "updated_id")
    private Integer updatedId;           // 수정자

    @Column(name = "updated_at")
    private LocalDate updatedAt;         // 수정일자

    @Builder
    public Trade(Integer projectId, Integer purchaseId, Integer sellId, Integer tradePrice, Integer tokenQuantity, LocalDate tradedAt, Integer createdId, LocalDate createdAt, Integer updatedId, LocalDate updatedAt) {
        this.projectId = projectId;
        this.purchaseId = purchaseId;
        this.sellId = sellId;
        this.tradePrice = tradePrice;
        this.tokenQuantity = tokenQuantity;
        this.tradedAt = tradedAt;
        this.createdId = createdId;
        this.createdAt = createdAt;
        this.updatedId = updatedId;
        this.updatedAt = updatedAt;
    }
}
