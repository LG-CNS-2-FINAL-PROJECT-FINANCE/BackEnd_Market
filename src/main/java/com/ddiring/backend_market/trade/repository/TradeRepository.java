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
    Optional<Trade> findByTradeId(Long tradeId);

    Boolean existsByTradeIdAndPurchaseIdAndSellIdAndTokenQuantity(Long tradeId, String purchaseId, String sellId, Integer tokenQuantity);

}
