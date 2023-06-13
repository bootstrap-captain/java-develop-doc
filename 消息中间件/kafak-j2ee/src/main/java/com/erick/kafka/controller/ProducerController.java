package com.erick.kafka.controller;

import com.erick.kafka.entity.ProduceEntitity;
import com.erick.kafka.service.ProducerService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/producer")
public class ProducerController {

    private final ProducerService producerService;

    @PostMapping("/async/send")
    public void asyncSend(ProduceEntitity entity) {
        producerService.asyncSend(entity.getTopicName(), entity.getPartition(),
                entity.getKey(), entity.getValue());
    }

    @PostMapping("/async/send/callback")
    public void asyncSendWithCallback(ProduceEntitity entity) {
        producerService.asyncSendWithCallBack(entity.getTopicName(), entity.getPartition(),
                entity.getKey(), entity.getValue());
    }

    @PostMapping("/sync/send")
    public void syncSend(ProduceEntitity entity) {
        producerService.syncSend(entity.getTopicName(), entity.getPartition(),
                entity.getKey(), entity.getKey());
    }

    @PostMapping("/batch/produce")
    public void batchProduce(String topicName, int batchSize) {
       producerService.batchProduce(topicName, batchSize);
    }
}
