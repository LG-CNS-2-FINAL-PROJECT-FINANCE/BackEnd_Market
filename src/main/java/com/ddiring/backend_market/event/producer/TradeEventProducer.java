package com.ddiring.backend_market.event.producer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class TradeEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void send(String topic, Object event) {
        try {
            kafkaTemplate.send(topic, event);
            log.info("거래 이벤트 전송 topic={}, payload={}", topic, event);
        } catch (Exception e) {
            log.error("거래 이벤트 전송 실패 topic={} event={}", topic, event, e);
        }
    }
}

