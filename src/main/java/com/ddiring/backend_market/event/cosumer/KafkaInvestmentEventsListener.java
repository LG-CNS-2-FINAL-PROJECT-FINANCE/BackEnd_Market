package com.ddiring.backend_market.event.cosumer;

import com.ddiring.backend_market.event.dto.InvestRequestAcceptedEvent;
import com.ddiring.backend_market.event.dto.InvestRequestRejectedEvent;
import com.ddiring.backend_market.event.dto.InvestSucceededEvent;
import com.ddiring.backend_market.event.dto.InvestFailedEvent;
import com.ddiring.backend_market.investment.entity.Investment;
import com.ddiring.backend_market.investment.repository.InvestmentRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaInvestmentEventsListener {

//    private final ObjectMapper objectMapper;
//    private final InvestmentRepository investmentRepository;
//
//    @KafkaListener(topics = "INVEST", groupId = "market-service-group")
//    public void listenInvestmentEvents(String message) {
//        try {
//            JsonNode node = objectMapper.readTree(message);
//            String eventType = node.get("eventType").asText();
//            log.info("[INVEST] 이벤트 수신: {}", eventType);
//
//            switch (eventType) {
//                case InvestRequestAcceptedEvent.EVENT_TYPE -> {
//                    InvestRequestAcceptedEvent evt = objectMapper.readValue(message, InvestRequestAcceptedEvent.class);
//                    handleRequestAccepted(evt);
//                }
//                case InvestRequestRejectedEvent.EVENT_TYPE -> {
//                    InvestRequestRejectedEvent evt = objectMapper.readValue(message, InvestRequestRejectedEvent.class);
//                    handleRequestRejected(evt);
//                }
//                case InvestSucceededEvent.EVENT_TYPE -> {
//                    InvestSucceededEvent evt = objectMapper.readValue(message, InvestSucceededEvent.class);
//                    handleInvestSucceeded(evt);
//                }
//                case InvestFailedEvent.EVENT_TYPE -> {
//                    InvestFailedEvent evt = objectMapper.readValue(message, InvestFailedEvent.class);
//                    handleInvestFailed(evt);
//                }
//                default -> log.warn("알 수 없는 투자 이벤트 타입: {}", eventType);
//            }
//        } catch (Exception e) {
//            log.error("투자 이벤트 처리 실패: {}", message, e);
//        }
//    }
//
//    private void handleRequestAccepted(InvestRequestAcceptedEvent event) {
//        Long id = event.getPayload().getInvestmentId();
//        investmentRepository.findById(id.intValue()).ifPresent(inv -> {
//            if (inv.getInvStatus() == Investment.InvestmentStatus.FUNDING
//                    || inv.getInvStatus() == Investment.InvestmentStatus.PENDING) {
//                inv.setInvStatus(Investment.InvestmentStatus.ALLOC_REQUESTED);
//                inv.setUpdatedAt(LocalDateTime.now());
//                investmentRepository.save(inv);
//            }
//        });
//    }
//
//    private void handleRequestRejected(InvestRequestRejectedEvent event) {
//        Long id = event.getPayload().getInvestmentId();
//        investmentRepository.findById(id.intValue()).ifPresent(inv -> {
//            if (inv.getInvStatus() != Investment.InvestmentStatus.COMPLETED) {
//                inv.setInvStatus(Investment.InvestmentStatus.FAILED);
//                inv.setFailureReason(event.getPayload().getReason());
//                inv.setUpdatedAt(LocalDateTime.now());
//                investmentRepository.save(inv);
//            }
//        });
//    }
//
//    private void handleInvestSucceeded(InvestSucceededEvent event) {
//        Long id = event.getPayload().getInvestmentId();
//        investmentRepository.findById(id.intValue()).ifPresent(inv -> {
//            if (inv.getInvStatus() != Investment.InvestmentStatus.COMPLETED) {
//                inv.setInvStatus(Investment.InvestmentStatus.COMPLETED);
//                inv.setTxHash(event.getPayload().getTxHash());
//                inv.setUpdatedAt(LocalDateTime.now());
//                investmentRepository.save(inv);
//            }
//        });
//    }
//
//    private void handleInvestFailed(InvestFailedEvent event) {
//        Long id = event.getPayload().getInvestmentId();
//        investmentRepository.findById(id.intValue()).ifPresent(inv -> {
//            if (inv.getInvStatus() != Investment.InvestmentStatus.COMPLETED) {
//                inv.setInvStatus(Investment.InvestmentStatus.FAILED);
//                String reason = event.getPayload().getErrorType();
//                if (event.getPayload().getErrorMessage() != null) {
//                    reason = reason + ":" + event.getPayload().getErrorMessage();
//                }
//                inv.setFailureReason(reason);
//                inv.setUpdatedAt(LocalDateTime.now());
//                investmentRepository.save(inv);
//            }
//        });
//    }
}
