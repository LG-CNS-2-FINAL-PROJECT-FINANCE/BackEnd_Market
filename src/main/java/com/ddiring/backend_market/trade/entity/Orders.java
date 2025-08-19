package com.ddiring.backend_market.trade.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "orders")
public class Orders {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "orders_id", nullable = false)
    private Integer ordersId;             // 주문 번호 (PK)

    @Column(name = "project_id", nullable = false)
    private String projectId;           // 프로젝트 번호 (FK)

    @Column(name = "user_seq", nullable = false)
    private String userSeq;             // 사용자 번호 (FK, 구매자)

    @Column(name = "orders_type", nullable = false)
    private Integer ordersType;           // 구매||판매

    @Column(name = "purchase_price", nullable = false)
    private Integer purchasePrice;       // 주문희망가

    @Column(name = "token_quantity", nullable = false)
    private Integer tokenQuantity;       // 주문수량

    @Column(name = "registed_at", nullable = false)
    private LocalDate registedAt;        // 등록일시

    @Column(name = "created_id")
    private Integer createdId;           // 생성자

    @Column(name = "created_at")
    private LocalDate createdAt;         // 생성일자

    @Column(name = "updated_id")
    private Integer updatedId;           // 수정자

    @Column(name = "updated_at")
    private LocalDate updatedAt;         // 수정일자

    @Builder
    public Orders(String projectId, String userSeq, Integer ordersType, Integer purchasePrice, Integer tokenQuantity, LocalDate registedAt, Integer createdId, LocalDate createdAt, Integer updatedId, LocalDate updatedAt) {
        this.projectId = projectId;
        this.userSeq = userSeq;
        this.ordersType = ordersType;
        this.purchasePrice = purchasePrice;
        this.tokenQuantity = tokenQuantity;
        this.registedAt = registedAt;
        this.createdId = createdId;
        this.createdAt = createdAt;
        this.updatedId = updatedId;
        this.updatedAt = updatedAt;
    }
    public void updateOrder(Integer purchasePrice, Integer tokenQuantity) {
        if (purchasePrice != null) {
            this.purchasePrice = purchasePrice;
        }
        if (tokenQuantity != null) {
            this.tokenQuantity = tokenQuantity;
        }
    }


}
