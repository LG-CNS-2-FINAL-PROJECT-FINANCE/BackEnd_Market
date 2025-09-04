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
    @Column(name = "history_seq" , nullable = false)
    private Integer historySeq;

    @Column(name = "project_id" )
    private String projectId;

    @Column(name = "user_Seq" )
    private String userSeq;

    @Column(name = "title" )
    private String title;

    @Column(name = "trade_type" )
    private Integer tradeType;

    @Column(name = "trade_price" )
    private Integer tradePrice;

    @Column(name = "token_quantity" )
    private Integer tokenQuantity;

    @Column(name = "per_price" )
    private Integer perPrice;

    @Column(name = "traded_at")
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
    public History(String projectId, String userSeq, String title, Integer tradeType, Integer tradePrice, Integer tokenQuantity, Integer perPrice, LocalDateTime tradedAt, Integer createdId, LocalDate createdAt, Integer updatedId, LocalDate updatedAt) {
        this.projectId = projectId;
        this.userSeq = userSeq;
        this.title = title;
        this.tradeType = tradeType;
        this.tradePrice = tradePrice;
        this.tokenQuantity = tokenQuantity;
        this.perPrice = perPrice;
        this.tradedAt = tradedAt;
        this.createdId = createdId;
        this.createdAt = createdAt;
        this.updatedId = updatedId;
        this.updatedAt = updatedAt;
    }

}
