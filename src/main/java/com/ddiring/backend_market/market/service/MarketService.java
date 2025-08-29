package com.ddiring.backend_market.market.service;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ddiring.backend_market.api.asset.AssetClient;
import com.ddiring.backend_market.api.asset.dto.request.SaveRecordRequest;
import com.ddiring.backend_market.market.dto.request.ProfitRequest;
import com.ddiring.backend_market.market.entity.Market;
import com.ddiring.backend_market.market.repository.MarketRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class MarketService {

    private final MarketRepository marketRepository;
    private final AssetClient assetClient;

    @Transactional
    public String distributeProfit(ProfitRequest profitRequest) {
        List<Market> markets = marketRepository.findByProjectId(profitRequest.getProjectId());

        markets.forEach(market -> {
            try {
                SaveRecordRequest req = new SaveRecordRequest();
                req.setAccount(null);
                req.setUserSeq(market.getUserSeq());
                req.setTransSeq(market.getTransSeq());
                req.setTransType(market.getTransType());
                req.setAmount(market.getAmount());
                assetClient.requestWithdrawal(req);
            } catch (Exception e) {
                log.error("수익금 분배 실패 projectId={} marketId={} error={}", market.getProjectId(),
                        market.getId(), e.getMessage());
            }
        });

        return "SUCCESS";
    }
}
