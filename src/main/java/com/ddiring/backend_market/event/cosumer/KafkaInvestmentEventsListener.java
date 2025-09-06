package com.ddiring.backend_market.event.cosumer;

import com.ddiring.backend_market.common.exception.NotFound;
import com.ddiring.backend_market.event.dto.*;
import com.ddiring.backend_market.investment.entity.Investment;
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
    public void listenInvestmentEvents(Map<String, Object> messageMap) {
        try {
//            Map<String, Object> messageMap = objectMapper.readValue(message, new TypeReference<>() {
//            });
            String eventType = (String) messageMap.get("eventType");
            if (eventType == null) {
                log.warn("eventType 필드를 찾을 수 없습니다: {}", messageMap);
                return;
            }

            log.info("[INVEST] 이벤트 수신: {}", eventType);
            switch (eventType) {
                case "INVESTMENT.REQUEST": {
                    InvestRequestEvent request = objectMapper.convertValue(messageMap, InvestRequestEvent.class);
                    if (request.getPayload() == null) {
                        log.warn("REQUEST 이벤트 payload 누락: {}",messageMap);
                        return;
                    }
                    handleRequest(request);
                    break;
                }
                case "INVESTMENT.REQUEST.ACCEPTED": {
                    InvestRequestAcceptedEvent accepted = objectMapper.convertValue(messageMap,
                            InvestRequestAcceptedEvent.class);
                    if (accepted.getPayload() == null) {
                        log.warn("ACCEPTED 이벤트 payload 누락: {}", messageMap);
                        return;
                    }
                    handleRequestAccepted(accepted);
                    break;
                }
                case "INVESTMENT.REQUEST.REJECTED": {
                    InvestRequestRejectedEvent rejected = objectMapper.convertValue(messageMap,
                            InvestRequestRejectedEvent.class);
                    if (rejected.getPayload() == null) {
                        log.warn("REJECTED 이벤트 payload 누락: {}", messageMap);
                        return;
                    }
                    handleRequestRejected(rejected);
                    break;
                }
                case "INVESTMENT.SUCCEEDED": {
                    InvestSucceededEvent succeeded = objectMapper.convertValue(messageMap,
                            InvestSucceededEvent.class);
                    if (succeeded.getPayload() == null) {
                        log.warn("SUCCEEDED 이벤트 payload 누락: {}", messageMap);
                        return;
                    }
                    handleInvestSucceeded(succeeded);
                    break;
                }
                case "INVESTMENT.FAILED": {
                    InvestFailedEvent failed = objectMapper.convertValue(messageMap,
                            InvestFailedEvent.class);
                    if (failed.getPayload() == null) {
                        log.warn("FAILED 이벤트 payload 누락: {}", messageMap);
                        return;
                    }
                    handleInvestFailed(failed);
                    break;
                }
                default:
                    log.warn("알 수 없는 투자 이벤트 타입: {}", eventType);
                    break;
            }
        } catch (Exception e) {
            log.error("투자 이벤트 처리 실패: {}", messageMap, e);
        }
    }

    @Transactional
    public void handleRequest(InvestRequestEvent event) {
        String projectId = event.getPayload().getProjectId();

        // 블록체인 토큰 이동 실제 요청 (요청 수락 후 실행)
        try {
            boolean bcRequested = investmentService.requestBlockchainTokenMove(projectId);
            log.info("[INVEST] 블록체인 토큰 이동 요청 결과 projectId={} requested={}", projectId, bcRequested);
        } catch (Exception e) {
            log.error("[INVEST] 블록체인 토큰 이동 요청 실패 projectId={} reason={}", projectId, e.getMessage());
        }
    }

    @Transactional
    public void handleRequestAccepted(InvestRequestAcceptedEvent event) {
        Long invesmtmentId = event.getPayload().getInvestmentId();

        Investment inv = investmentRepository.findByInvestmentSeq(invesmtmentId.intValue())
                .orElseThrow(() -> new NotFound("찾을 수 없는 투자 번호 입니다."));

        if (!InvestmentStatus.FUNDING.equals(inv.getInvStatus())) {
            log.warn("잘못된 상태의 투자에 대한 할당 수락 이벤트 수신. investmentId={}, status={}", inv.getInvestmentSeq(),
                    inv.getInvStatus());
            throw new IllegalStateException("할당 요청을 수락하기에 올바르지 않은 투자 상태입니다: " + inv.getInvStatus());
        }

        inv.setInvStatus(InvestmentStatus.ALLOC_REQUESTED);
        inv.setUpdatedAt(LocalDateTime.now());

        investmentRepository.save(inv);
    }

    @Transactional
    public void handleRequestRejected(InvestRequestRejectedEvent event) {
        Long invesmtmentId = event.getPayload().getInvestmentId();

        Investment inv = investmentRepository.findByInvestmentSeq(invesmtmentId.intValue())
                .orElseThrow(() -> new NotFound("찾을 수 없는 투자 번호 입니다."));

        if (!InvestmentStatus.ALLOC_REQUESTED.equals(inv.getInvStatus())) {
            throw new IllegalStateException("할당 요청 중이 아닌 투자 번호입니다.");
        }

        inv.setInvStatus(InvestmentStatus.REJECTED);
        inv.setUpdatedAt(LocalDateTime.now());

        investmentRepository.save(inv);
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
