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
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaInvestmentEventsListener {

    private final ObjectMapper objectMapper;
    private final InvestmentRepository investmentRepository;
    private final InvestmentService investmentService;

    @KafkaListener(topics = "INVEST", groupId = "market-service-group")
    public void listenInvestmentEvents(String message) {
        try {
            Map<String, Object> messageMap = objectMapper.readValue(message, new TypeReference<>() {});
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
                    // ▼▼▼ 1. 로직이 분리된 메서드를 호출하도록 변경 ▼▼▼
                    processRequestAccepted(accepted);
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

    // --- ▼▼▼ 2. 'ACCEPTED' 이벤트 처리 로직 분리 ▼▼▼ ---

    /**
     * 'ACCEPTED' 이벤트의 전체 처리를 조율하는 메서드 (트랜잭션 없음)
     */
    public void processRequestAccepted(InvestRequestAcceptedEvent event) {
        String projectId = event.getPayload().getProjectId();

        // 2-1. DB 상태 업데이트를 먼저 실행하고 트랜잭션을 커밋합니다.
        updateStatusForAcceptedRequest(projectId);

        // 2-2. DB 트랜잭션이 끝난 후, 외부 블록체인 API를 호출합니다.
        triggerBlockchainTokenMove(projectId);
    }

    /**
     * DB 상태 변경만을 담당하는 메서드 (트랜잭션 적용)
     */
    @Transactional
    public void updateStatusForAcceptedRequest(String projectId) {
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
    }

    /**
     * 블록체인 API 호출만을 담당하는 메서드 (트랜잭션 없음)
     */
    public void triggerBlockchainTokenMove(String projectId) {
        try {
            boolean bcRequested = investmentService.requestBlockchainTokenMove(projectId);
            log.info("[INVEST] 블록체인 토큰 이동 요청 결과 projectId={} requested={}", projectId, bcRequested);
        } catch (Exception e) {
            log.error("[INVEST] 블록체인 토큰 이동 요청 실패 projectId={} reason={}", projectId, e.getMessage());
            // TODO: 실패 시 재시도 또는 관리자 알림 등의 보상 로직이 필요할 수 있습니다.
        }
    }

    // --- ▲▲▲ 'ACCEPTED' 이벤트 처리 로직 분리 끝 ▲▲▲ ---

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

    // ▼▼▼ 3. @Transactional 추가 ▼▼▼
    @Transactional
    public void handleInvestSucceeded(InvestSucceededEvent event) {
        Long id = event.getPayload().getInvestmentId();
        investmentRepository.findById(id.intValue()).ifPresent(inv -> {
            if (inv.getInvStatus() != InvestmentStatus.COMPLETED) {
                inv.setInvStatus(InvestmentStatus.COMPLETED);
                inv.setUpdatedAt(LocalDateTime.now());
                investmentRepository.save(inv);
            }
        });
    }

    // ▼▼▼ 4. @Transactional 추가 및 public으로 변경 ▼▼▼
    @Transactional
    public void handleInvestFailed(InvestFailedEvent event) {
        Long id = event.getPayload().getInvestmentId();
        investmentRepository.findById(id.intValue()).ifPresent(inv -> {
            if (inv.getInvStatus() != InvestmentStatus.COMPLETED) {
                String reason = event.getPayload().getErrorType();
                if (event.getPayload().getErrorMessage() != null) {
                    reason = reason + ":" + event.getPayload().getErrorMessage();
                }
                // TODO: 'reason' 변수를 inv 객체에 저장하는 로직이 필요해 보입니다.
                inv.setInvStatus(InvestmentStatus.FAILED);
                inv.setUpdatedAt(LocalDateTime.now());
                investmentRepository.save(inv);
            }
        });
    }
}