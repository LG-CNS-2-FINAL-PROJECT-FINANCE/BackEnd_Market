package com.ddiring.backend_market.investment.repository;

import com.ddiring.backend_market.investment.entity.Investment;
import com.ddiring.backend_market.investment.entity.Investment.InvestmentStatus;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InvestmentRepository extends JpaRepository<Investment, Integer> {

    // 개인 투자 내역 조회
    List<Investment> findByUserSeq(String userSeq);

    // 개인 취소 내역
    List<Investment> findByUserSeqAndProjectIdAndInvStatus(String userSeq, String projectId,
            InvestmentStatus invStatus);

    // 프로젝트별 투자자 조회
    List<Investment> findByProjectId(String projectId);
}