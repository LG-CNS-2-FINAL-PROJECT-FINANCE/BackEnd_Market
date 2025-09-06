package com.ddiring.backend_market.investment.scheduler;

import com.ddiring.backend_market.api.product.ProductClient;
import com.ddiring.backend_market.api.product.ProductDTO;
import com.ddiring.backend_market.investment.service.InvestmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

// 토큰 할당 스케줄러
@Slf4j
@Component
@RequiredArgsConstructor
public class AllocationScheduler {

    private final ProductClient productClient;
    private final InvestmentService investmentService;

    // 매일 00:05 실행
    @Scheduled(cron = "0 5 0 * * *")
    public void dailyAllocationCheck() {
        LocalDate today = LocalDate.now();
        List<ProductDTO> products;
        try {
            products = Optional.ofNullable(productClient.getAllUnOpenProduct())
                    .map(ResponseEntity::getBody)
                    .orElse(List.of());
        } catch (Exception e) {
            log.error("[AllocationScheduler] 상품 전체 조회 실패 reason={}", e.getMessage());
            return;
        }

        products.stream()
                .filter(p -> p != null && p.getProjectId() != null)
                .filter(p -> p.getEndDate() != null && !today.isBefore(p.getEndDate()))
                .forEach(p -> {
                    try {
                        boolean sent = investmentService.triggerAllocationIfEligible(p.getProjectId());
                        if (sent) {
                            log.info("[AllocationScheduler] 할당 이벤트 발행 projectId={}", p.getProjectId());
                        }
                    } catch (Exception ex) {
                        log.error("[AllocationScheduler] 할당 트리거 실패 projectId={} error={}", p.getProjectId(),
                                ex.getMessage());
                    }
                });
    }
}
