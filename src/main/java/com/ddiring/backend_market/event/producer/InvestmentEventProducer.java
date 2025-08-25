package com.ddiring.backend_market.event.producer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class InvestmentEventProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public void send(String topic, Object event) {
        try {
            String payload = objectMapper.writeValueAsString(event);
            kafkaTemplate.send(topic, payload);
            log.info("투자 이벤트 전송 topic={}, payload={}", topic, payload);
        } catch (JsonProcessingException e) {
            log.error("이벤트 직렬화 실패 topic={} event={}", topic, event, e);
            throw new IllegalStateException("이벤트 직렬화 실패", e);
        }
    }
}
