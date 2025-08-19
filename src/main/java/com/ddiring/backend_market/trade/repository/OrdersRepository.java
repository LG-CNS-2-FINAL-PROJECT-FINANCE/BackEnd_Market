package com.ddiring.backend_market.trade.repository;

import com.ddiring.backend_market.trade.entity.Orders;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrdersRepository extends JpaRepository<Orders, Integer> {

    List<Orders> findByProjectIdAndOrdersTypeOrderByPurchasePriceAscRegistedAtAsc(String projectId, Integer ordersType);
    List<Orders> findByProjectIdAndOrdersTypeOrderByPurchasePriceDescRegistedAtAsc(String projectId, Integer ordersType);
    Optional<Orders> findByOrdersIdAndUserSeqAndProjectId(Integer ordersId, String userSeq, String projectId);
    List<Orders> findByUserSeq(String userSeq);

}
