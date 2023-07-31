package com.erick.boot.kafka.controller;

import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.CompletableFuture;

@RequestMapping("/produce")
@RestController
@RequiredArgsConstructor
public class ProducerController {
    private final KafkaTemplate<String, String> kafkaTemplate;

    @GetMapping("/send")
    public void send(String topicName, String key, String data) {
        ProducerRecord<String, String> producerRecord = new ProducerRecord(topicName, key, data);
        CompletableFuture<SendResult<String, String>> send = kafkaTemplate.send(producerRecord);
    }

    @GetMapping("/send/batch/random/")
    public void sendBatch(String topicName, int size) {
        for (int i = 0; i < size; i++) {
            kafkaTemplate.send(topicName, "hello-erick==" + System.currentTimeMillis());
        }
    }
}
