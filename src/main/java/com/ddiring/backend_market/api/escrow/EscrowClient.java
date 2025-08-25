package com.ddiring.backend_market.api.escrow;

import com.ddiring.backend_market.api.escrow.dto.SettleTradeRequestDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "escrow-service", url = "${escrow-service.url}")
public interface EscrowClient {

    @PostMapping("/api/escrow/internal/settle-trade")
    void settleTrade(@RequestBody SettleTradeRequestDto requestDto);
}