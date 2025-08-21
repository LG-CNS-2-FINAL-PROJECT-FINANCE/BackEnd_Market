package com.ddiring.backend_market.trade.repository;

import com.ddiring.backend_market.trade.entity.Trade;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TradeRepository extends JpaRepository<Trade, Integer> {
    List<Trade> findTop20ByProjectIdOrderByTradedAtDesc(String projectId);
    List<Trade> findByPurchaseIdOrSellId(Integer purchaseId, Integer sellId);
    Optional<Trade> findByTradeId(Long tradeId);
    @Modifying
    @Query("update Trade t set t.tradeStatus = ?2 where t.tradeSeq = ?1")
    void updateTradeStatus(Integer tradeId, String tradeStatus);
}
