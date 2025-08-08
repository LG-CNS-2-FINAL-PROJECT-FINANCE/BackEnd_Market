package com.ddiring.backend_market.trade.repository;

import com.ddiring.backend_market.trade.entity.Orders;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrdersRepository extends JpaRepository<Orders, Integer> {
    // projectId와 ordersType이 0(판매)인 주문 중, purchasePrice 오름차순(최저가)으로 정렬하여 조회
    List<Orders> findByProjectIdAndOrdersTypeOrderByPurchasePriceAscRegistedAtAsc(String projectId, Integer ordersType);
    // projectId와 ordersType이 1(구매)인 주문 중, purchasePrice 내림차순(최고가)으로 정렬하여 조회
    List<Orders> findByProjectIdAndOrdersTypeOrderByPurchasePriceDescRegistedAtAsc(String projectId, Integer ordersType);
    Optional<Orders> findByOrdersIdAndUserSeqAndProjectId(Integer ordersId, Integer userSeq, String projectId);

    List<Orders> findByUserSeq(Integer userSeq);

}
