package com.ddiring.backend_market.investment.repository;

import com.ddiring.backend_market.investment.entity.Investment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InvestmentRepository extends JpaRepository<Investment, Integer> {

    // 투자 상품 전체 조회
    List<Investment> findByProductId(Integer productId);
}
