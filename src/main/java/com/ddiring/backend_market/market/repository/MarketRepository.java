package com.ddiring.backend_market.market.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import com.ddiring.backend_market.market.entity.Market;

public interface MarketRepository extends JpaRepository<Market, Integer> {

    List<Market> findByProjectId(String projectId);
}
