package com.ddiring.backend_market.investment.repository;

import com.ddiring.backend_market.investment.entity.Investment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DetailedInvestmentRepository extends JpaRepository<Investment, Integer> {

}
