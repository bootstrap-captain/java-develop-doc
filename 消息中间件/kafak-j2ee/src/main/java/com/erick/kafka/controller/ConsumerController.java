package com.erick.kafka.controller;

import com.erick.kafka.service.ConsumerService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/consume")
@RequiredArgsConstructor
public class ConsumerController {

    private final ConsumerService consumerService;

    @GetMapping("/byTopic")
    public void consumeTopic(String topicName) {
        consumerService.consumeTopic(topicName);
    }

    @GetMapping("/byTopicPartition")
    public void consumeTopicPartition(String topicName, Integer partitionNo) {
        consumerService.consumeTopicPartition(topicName, partitionNo);
    }

    @GetMapping("/byTopic/mulit/consumer")
    public void consumeTopicByMultiConsumer(String topicName) {
        consumerService.consumeWithMultiConsumer(topicName);
    }
}
