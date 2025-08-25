package com.ddiring.backend_market.trade.repository;

import com.ddiring.backend_market.trade.entity.Orders;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrdersRepository extends JpaRepository<Orders, Integer> {

    List<Orders> findByProjectIdAndOrdersTypeOrderByPurchasePriceAscRegistedAtAsc(String projectId, Integer ordersType);
    List<Orders> findByProjectIdAndOrdersTypeOrderByPurchasePriceDescRegistedAtAsc(String projectId, Integer ordersType);
    List<Orders> findByProjectIdAndOrdersTypeAndOrdersStatusOrderByPurchasePriceAscRegistedAtAsc(String projectId, int ordersType, String ordersStatus);
    List<Orders> findByProjectIdAndOrdersTypeAndOrdersStatusOrderByPurchasePriceDescRegistedAtAsc(String projectId, int ordersType, String ordersStatus);

    // -- 기존 메소드들 --
    Optional<Orders> findByOrdersIdAndUserSeqAndProjectId(Integer ordersId, String userSeq, String projectId);
    List<Orders> findByUserSeq(String userSeq);
    Optional<Orders> findByOrdersIdAndUserSeq(Integer orderId, String userSeq);

}
