package com.ddiring.backend_market.investment.repository;

import com.ddiring.backend_market.investment.entity.Investment;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InvestmentRepository extends JpaRepository<Investment, Integer> {

    // 개인 투자 내역 조회
    List<Investment> findByUserSeq(String userSeq);

    // 주문 상태 확인
    List<Investment> findByUserSeqAndProjectId(String userSeq, String projectId);

    // 프로젝트별 투자자 조회
    List<Investment> findByProjectId(String projectId);

    List<Investment> findByInvestmentSeqIn(List<Integer> investmentSeq);
}