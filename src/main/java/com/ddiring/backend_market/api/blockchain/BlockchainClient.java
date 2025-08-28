package com.ddiring.backend_market.api.blockchain;

import com.ddiring.backend_market.api.blockchain.dto.request.InvestmentDto;
import com.ddiring.backend_market.api.blockchain.dto.trade.TradeDto;
import com.ddiring.backend_market.common.dto.ApiResponseDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "blockchainClient", url = "${blockchain.base-url}")
public interface BlockchainClient {

    @PostMapping("/api/contract/investment")
    ApiResponseDto<?> requestInvestmentTokenMove(@RequestBody InvestmentDto investmentDto);

    @PostMapping("/api/contract/trade")
    ApiResponseDto<?> requestTradeTokenMove(@RequestBody TradeDto tradeDto);
}
