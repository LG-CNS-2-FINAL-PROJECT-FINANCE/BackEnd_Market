package com.ddiring.backend_market.event.cosumer;

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
                case "INVESTMENT.REQUEST": {
                    InvestRequestEvent request = objectMapper.convertValue(messageMap, InvestRequestEvent.class);
                    if (request.getPayload() == null) {
                        log.warn("REQUEST 이벤트 payload 누락: {}", message);
                        return;
                    }
                    handleRequest(request);
                    break;
                }
                case "INVESTMENT.REQUEST.ACCEPTED": {
                    InvestRequestAcceptedEvent accepted = objectMapper.convertValue(messageMap,
                            InvestRequestAcceptedEvent.class);
                    if (accepted.getPayload() == null) {
                        log.warn("ACCEPTED 이벤트 payload 누락: {}", message);
                        return;
                    }
                    handleRequestAccepted(accepted);
                    break;
                }
                case "INVESTMENT.REQUEST.REJECTED": {
                    InvestRequestRejectedEvent rejected = objectMapper.convertValue(messageMap,
                            InvestRequestRejectedEvent.class);
                    if (rejected.getPayload() == null) {
                        log.warn("REJECTED 이벤트 payload 누락: {}", message);
                        return;
                    }
                    handleRequestRejected(rejected);
                    break;
                }
                case "INVESTMENT.SUCCEEDED": {
                    InvestSucceededEvent succeeded = objectMapper.convertValue(messageMap,
                            InvestSucceededEvent.class);
                    if (succeeded.getPayload() == null) {
                        log.warn("SUCCEEDED 이벤트 payload 누락: {}", message);
                        return;
                    }
                    handleInvestSucceeded(succeeded);
                    break;
                }
                case "INVESTMENT.FAILED": {
                    InvestFailedEvent failed = objectMapper.convertValue(messageMap,
                            InvestFailedEvent.class);
                    if (failed.getPayload() == null) {
                        log.warn("FAILED 이벤트 payload 누락: {}", message);
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
            log.error("투자 이벤트 처리 실패: {}", message, e);
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
        Long investmentId = event.getPayload().getInvestmentId();

        Investment inv = investmentRepository.findByInvestmentSeq(investmentId.intValue())
                .orElse(null);

        if (inv == null) {
            log.warn("존재하지 않는 투자 번호에 대한 할당 수락 이벤트 수신. investmentId={}", investmentId);
            return;
        }

        if (InvestmentStatus.ALLOC_REQUESTED.equals(inv.getInvStatus())) {
            log.info("이미 할당 요청된 투자. 중복 이벤트 무시. investmentId={}", investmentId);
            return;
        }

        if (InvestmentStatus.COMPLETED.equals(inv.getInvStatus()) ||
                InvestmentStatus.REJECTED.equals(inv.getInvStatus()) ||
                InvestmentStatus.FAILED.equals(inv.getInvStatus())) {
            log.warn("이미 완료된 투자에 대한 할당 수락 이벤트 수신. investmentId={}, status={}", investmentId, inv.getInvStatus());
            return;
        }

        if (!InvestmentStatus.FUNDING.equals(inv.getInvStatus())) {
            log.warn("잘못된 상태의 투자에 대한 할당 수락 이벤트 수신. investmentId={}, status={}", investmentId, inv.getInvStatus());
            return;
        }

        // 블록체인 검증 상태 확인 로직 추가
        String status = event.getPayload().getStatus();
        if ("FAILED".equals(status)) {
            log.warn("블록체인에서 거절된 투자 요청. investmentId={} status={}", investmentId, status);
            inv.setInvStatus(InvestmentStatus.REJECTED);
            inv.setUpdatedAt(LocalDateTime.now());
            investmentRepository.save(inv);
            log.info("투자 할당 거절 처리 완료. investmentId={}", investmentId);
            return;
        }

        inv.setInvStatus(InvestmentStatus.ALLOC_REQUESTED);
        inv.setUpdatedAt(LocalDateTime.now());
        investmentRepository.save(inv);

        log.info("투자 할당 요청 처리 완료. investmentId={} status={}", investmentId, status);
    }

    @Transactional
    public void handleRequestRejected(InvestRequestRejectedEvent event) {
        Long investmentId = event.getPayload().getInvestmentId();

        // 1) 투자 번호 존재 여부 확인
        Investment inv = investmentRepository.findByInvestmentSeq(investmentId.intValue())
                .orElse(null);

        if (inv == null) {
            log.warn("존재하지 않는 투자 번호에 대한 할당 거절 이벤트 수신. investmentId={}", investmentId);
            return; // 예외를 발생시키지 않고 무시
        }

        // 2) 상태 확인 및 멱등성 보장
        if (InvestmentStatus.REJECTED.equals(inv.getInvStatus())) {
            log.info("이미 거절된 투자. 중복 이벤트 무시. investmentId={}", investmentId);
            return; // 이미 처리됨, 중복 이벤트 무시
        }

        if (InvestmentStatus.COMPLETED.equals(inv.getInvStatus()) ||
                InvestmentStatus.FAILED.equals(inv.getInvStatus())) {
            log.warn("이미 완료된 투자에 대한 할당 거절 이벤트 수신. investmentId={}, status={}", investmentId, inv.getInvStatus());
            return; // 이미 최종 상태, 무시
        }

        if (!InvestmentStatus.ALLOC_REQUESTED.equals(inv.getInvStatus())) {
            log.warn("할당 요청 중이 아닌 투자에 대한 거절 이벤트 수신. investmentId={}, status={}", investmentId, inv.getInvStatus());
            return; // 예외 발생 대신 무시
        }

        // 3) 정상적인 상태 변경
        inv.setInvStatus(InvestmentStatus.REJECTED);
        inv.setUpdatedAt(LocalDateTime.now());
        investmentRepository.save(inv);

        log.info("투자 할당 거절 처리 완료. investmentId={}", investmentId);
    }

    @Transactional
    public void handleInvestSucceeded(InvestSucceededEvent event) {
        Long investmentId = event.getPayload().getInvestmentId();

        investmentRepository.findById(investmentId.intValue()).ifPresentOrElse(inv -> {
            // 1) 이미 완료된 경우 중복 이벤트 무시
            if (InvestmentStatus.COMPLETED.equals(inv.getInvStatus())) {
                log.info("이미 완료된 투자. 중복 이벤트 무시. investmentId={}", investmentId);
                return;
            }

            // 2) 이미 다른 최종 상태인 경우 경고
            if (InvestmentStatus.REJECTED.equals(inv.getInvStatus()) ||
                    InvestmentStatus.FAILED.equals(inv.getInvStatus())) {
                log.warn("이미 완료된 투자에 대한 성공 이벤트 수신. investmentId={}, status={}", investmentId, inv.getInvStatus());
                return;
            }

            // 3) 정상적인 완료 처리
            Long tokenAmount = event.getPayload().getTokenAmount();
            if (tokenAmount != null) {
                inv.setTokenQuantity(tokenAmount.intValue());
            }
            inv.setInvStatus(InvestmentStatus.COMPLETED);
            inv.setUpdatedAt(LocalDateTime.now());
            investmentRepository.save(inv);

            log.info("[INVEST] 투자 완료 처리 완료. investmentId={} tokenQuantity={}", investmentId, inv.getTokenQuantity());
        }, () -> {
            log.warn("존재하지 않는 투자 번호에 대한 성공 이벤트 수신. investmentId={}", investmentId);
        });
    }

    @Transactional
    public void handleInvestFailed(InvestFailedEvent event) {
        Long investmentId = event.getPayload().getInvestmentId();

        investmentRepository.findById(investmentId.intValue()).ifPresentOrElse(inv -> {
            // 1) 이미 실패한 경우 중복 이벤트 무시
            if (InvestmentStatus.FAILED.equals(inv.getInvStatus())) {
                log.info("이미 실패한 투자. 중복 이벤트 무시. investmentId={}", investmentId);
                return;
            }

            // 2) 이미 다른 최종 상태인 경우 경고
            if (InvestmentStatus.COMPLETED.equals(inv.getInvStatus()) ||
                    InvestmentStatus.REJECTED.equals(inv.getInvStatus())) {
                log.warn("이미 완료된 투자에 대한 실패 이벤트 수신. investmentId={}, status={}", investmentId, inv.getInvStatus());
                return;
            }

            // 3) 정상적인 실패 처리
            String reason = event.getPayload().getErrorType();
            if (event.getPayload().getErrorMessage() != null) {
                reason = reason + ":" + event.getPayload().getErrorMessage();
            }
            inv.setInvStatus(InvestmentStatus.FAILED);
            inv.setUpdatedAt(LocalDateTime.now());
            investmentRepository.save(inv);

            log.info("[INVEST] 투자 실패 처리 완료. investmentId={} reason={}", investmentId, reason);
        }, () -> {
            log.warn("존재하지 않는 투자 번호에 대한 실패 이벤트 수신. investmentId={}", investmentId);
        });
    }
}
