package com.ddiring.backend_market.investment.repository;

import com.ddiring.backend_market.investment.entity.Investment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BuyInvestmentRepository extends JpaRepository<Investment, Integer> {
}
