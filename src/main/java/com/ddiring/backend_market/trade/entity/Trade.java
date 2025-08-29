package com.ddiring.backend_market.trade.entity;

import com.ddiring.backend_market.investment.entity.Investment;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "trade")
public class Trade {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "trade_id", nullable = false)
    private Long tradeId;

    @Column(name = "project_id", nullable = false)
    private String projectId;

    @Column(name = "purchase_id", nullable = false)
    private Integer purchaseId;

    @Column(name = "sell_id", nullable = false)
    private Integer sellId;

    @Column(name = "trade_status")
    private String tradeStatus;

    @Column(name = "title")
    private String title;

    @Column(name = "buyer_address")
    private String buyerAddress;

    @Column(name = "seller_address")
    private String sellerAddress;

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
    public Trade(String projectId, Integer purchaseId, Integer sellId, Integer tradePrice, Integer tokenQuantity, String tradeStatus, LocalDateTime tradedAt, String buyerAddress, String sellerAddress, Integer createdId, LocalDate createdAt, Integer updatedId, LocalDate updatedAt) {
        this.projectId = projectId;
        this.purchaseId = purchaseId;
        this.sellId = sellId;
        this.tradePrice = tradePrice;
        this.tradeStatus = tradeStatus;
        this.tokenQuantity = tokenQuantity;
        this.tradedAt = tradedAt;
        this.buyerAddress = buyerAddress;
        this.sellerAddress = sellerAddress;
        this.createdId = createdId;
        this.createdAt = createdAt;
        this.updatedId = updatedId;
        this.updatedAt = updatedAt;
    }
}
