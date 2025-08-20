package com.ddiring.backend_market.event.producer;// KafkaProducerService.java
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class KafkaProducerService {

    private static final String TOPIC_NAME = "trade-events-topic";

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    public void sendTradeEvent(String message) {
        kafkaTemplate.send(TOPIC_NAME, message);
        System.out.println("Sent message to Kafka: " + message);
    }
}