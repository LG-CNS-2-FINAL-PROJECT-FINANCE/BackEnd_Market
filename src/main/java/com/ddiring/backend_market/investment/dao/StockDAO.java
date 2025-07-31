package com.ddiring.backend_market.investment.dao;

import com.ddiring.backend_market.common.exception.BuyException;
import com.ddiring.backend_market.common.exception.NotFound;
import com.ddiring.backend_market.investment.entity.Investment;

import java.util.List;

public interface StockDAO {

    List<Investment> investmentALl() throws NotFound;

    int
}
