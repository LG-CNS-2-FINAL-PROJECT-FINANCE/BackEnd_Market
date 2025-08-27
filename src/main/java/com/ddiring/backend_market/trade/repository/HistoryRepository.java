package com.ddiring.backend_market.trade.repository;

import com.ddiring.backend_market.trade.entity.History;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HistoryRepository extends JpaRepository<History, Integer> {
    List<History> findByUserSeqOrderByTradedAtDesc(String userSeq);
    List<History> findByUserSeqAndProjectIdOrderByTradedAtDesc(String userSeq, String projectId);
    List<History> findAllByOrderByTradedAtDesc();
}