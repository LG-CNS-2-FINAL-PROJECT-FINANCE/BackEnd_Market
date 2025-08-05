package com.ddiring.backend_market.investment.repository;

import com.ddiring.backend_market.investment.entity.Investment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InvestmentRepository extends JpaRepository<Investment, Integer> {

    // 개인 투자 내역 조회
    List<Investment> findByUserSeq(Integer userSeq);

    // 프로젝트별 투자자 조회
    List<Investment> findByProjectId(Integer projectId);

    // 상품 상태 조회
    List<Investment> findByInvStatus(Investment.InvestmentStatus investmentStatus);
}