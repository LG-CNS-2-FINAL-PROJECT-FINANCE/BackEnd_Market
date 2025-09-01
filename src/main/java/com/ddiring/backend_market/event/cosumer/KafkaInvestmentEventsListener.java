package com.ddiring.backend_market.event.cosumer;

import com.ddiring.backend_market.event.dto.InvestRequestAcceptedEvent;
import com.ddiring.backend_market.event.dto.InvestRequestRejectedEvent;
import com.ddiring.backend_market.event.dto.InvestSucceededEvent;
import com.ddiring.backend_market.event.dto.InvestFailedEvent;
import com.ddiring.backend_market.investment.entity.Investment.InvestmentStatus;
import com.ddiring.backend_market.investment.repository.InvestmentRepository;
import com.ddiring.backend_market.investment.service.InvestmentService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaInvestmentEventsListener {

    private final ObjectMapper objectMapper;
    private final InvestmentRepository investmentRepository;
    private final InvestmentService investmentService;

    @KafkaListener(topics = "INVESTMENT", groupId = "market-service-group")
    public void listenInvestmentEvents(String message) {
        try {
            Map<String, Object> messageMap = objectMapper.readValue(message, new TypeReference<>() {
            });
            String eventType = (String) messageMap.get("eventType");
            if (eventType == null) {
                log.warn("eventType 필드를 찾을 수 없습니다: {}", message);
                return;
            }

            log.info("[INVEST] 이벤트 수신: {}", eventType);
            switch (eventType) {
                case "INVESTMENT.REQUEST.ACCEPTED": {
                    InvestRequestAcceptedEvent accepted = objectMapper.convertValue(messageMap.get("payload"),
                            InvestRequestAcceptedEvent.class);
                    handleRequestAccepted(accepted);
                    break;
                }
                case "INVESTMENT.REQUEST.REJECTED": {
                    InvestRequestRejectedEvent rejected = objectMapper.convertValue(messageMap.get("payload"),
                            InvestRequestRejectedEvent.class);
                    handleRequestRejected(rejected);
                    break;
                }
                case "INVESTMENT.SUCCEEDED": {
                    InvestSucceededEvent succeeded = objectMapper.convertValue(messageMap.get("payload"),
                            InvestSucceededEvent.class);
                    handleInvestSucceeded(succeeded);
                    break;
                }
                case "INVESTMENT.FAILED": {
                    InvestFailedEvent failed = objectMapper.convertValue(messageMap.get("payload"),
                            InvestFailedEvent.class);
                    handleInvestFailed(failed);
                    break;
                }
                default:
                    log.warn("알 수 없는 투자 이벤트 타입: {}", eventType);
                    break;
            }
        } catch (Exception e) {
            log.error("투자 이벤트 처리 실패: {}", message, e);
        }
    }

    @Transactional
    public void handleRequestAccepted(InvestRequestAcceptedEvent event) {
        String projectId = event.getPayload().getProjectId();
        var list = investmentRepository.findByProjectId(projectId).stream()
                .filter(inv -> inv.getInvStatus() == InvestmentStatus.FUNDING
                        || inv.getInvStatus() == InvestmentStatus.PENDING)
                .peek(inv -> {
                    inv.setInvStatus(InvestmentStatus.ALLOC_REQUESTED);
                    inv.setUpdatedAt(LocalDateTime.now());
                })
                .toList();
        if (!list.isEmpty()) {
            investmentRepository.saveAll(list);
        }
        // 블록체인 토큰 이동 실제 요청 (요청 수락 후 실행)
        try {
            boolean bcRequested = investmentService.requestBlockchainTokenMove(projectId);
            log.info("[INVEST] 블록체인 토큰 이동 요청 결과 projectId={} requested={}", projectId, bcRequested);
        } catch (Exception e) {
            log.error("[INVEST] 블록체인 토큰 이동 요청 실패 projectId={} reason={}", projectId, e.getMessage());
        }
    }

    @Transactional
    public void handleRequestRejected(InvestRequestRejectedEvent event) {
        String projectId = event.getPayload().getProjectId();
        var list = investmentRepository.findByProjectId(projectId).stream()
                .filter(inv -> inv.getInvStatus() != InvestmentStatus.COMPLETED)
                .peek(inv -> {
                    inv.setInvStatus(InvestmentStatus.REJECTED);
                    inv.setUpdatedAt(LocalDateTime.now());
                })
                .toList();
        if (!list.isEmpty()) {
            investmentRepository.saveAll(list);
        }
    }

    public void handleInvestSucceeded(InvestSucceededEvent event) {
        Long id = event.getPayload().getInvestmentId();
        investmentRepository.findById(id.intValue()).ifPresent(inv -> {
            if (inv.getInvStatus() != InvestmentStatus.COMPLETED) {
                Long tokenAmount = event.getPayload().getTokenAmount();
                if (tokenAmount != null) {
                    inv.setTokenQuantity(tokenAmount.intValue());
                }
                inv.setInvStatus(InvestmentStatus.COMPLETED);
                inv.setUpdatedAt(LocalDateTime.now());
                investmentRepository.save(inv);
                log.info("[INVEST] 투자 완료 처리 investmentId={} tokenQuantity={}", id, inv.getTokenQuantity());
            }
        });
    }

    public void handleInvestFailed(InvestFailedEvent event) {
        Long id = event.getPayload().getInvestmentId();
        investmentRepository.findById(id.intValue()).ifPresent(inv -> {
            if (inv.getInvStatus() != InvestmentStatus.COMPLETED) {
                String reason = event.getPayload().getErrorType();
                if (event.getPayload().getErrorMessage() != null) {
                    reason = reason + ":" + event.getPayload().getErrorMessage();
                }
                inv.setInvStatus(InvestmentStatus.FAILED);
                inv.setUpdatedAt(LocalDateTime.now());
                investmentRepository.save(inv);
            }
        });
    }
}
