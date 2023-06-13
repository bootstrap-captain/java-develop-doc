package com.erick.boot.kafka.controller;

import com.erick.boot.kafka.service.MessageProcessor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;

import javax.annotation.Resource;

@Configuration
@Slf4j
public class ConsumeListener {

    @Resource
    private MessageProcessor processor;


    /**
     * 可以监听多个topic
     *
     * @param acknowledgment
     */
    @KafkaListener(topics = {"apple"}, containerFactory = "kafkaListenerContainerFactory", concurrency = "3")
    public <T> void consumeTopic(ConsumerRecord<String, Object> consumerRecord, Acknowledgment acknowledgment) {
        try {
            System.out.println("comming");
            //processor.processMessage(consumerRecord.toString());
        } catch (Exception e) {
            log.info("message processed successfully");
        } finally {
            long start = System.currentTimeMillis();
            acknowledgment.acknowledge();
            System.out.println("ERICK-TIME:" + (System.currentTimeMillis() - start));
        }
    }
}
