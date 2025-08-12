package com.ddiring.backend_market.trade.repository;

import com.ddiring.backend_market.trade.entity.Trade;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TradeRepository extends JpaRepository<Trade, Integer> {
    List<Trade> findTop20ByProjectIdOrderByTradedAtDesc(String projectId); // 특정 프로젝트의 최근 20개 체결 내역 조회
    List<Trade> findByPurchaseIdOrSellId(Integer purchaseId, Integer sellId); // 개인 체결 내역 조회 (created_id는 체결을 발생시킨 사용자)
}
