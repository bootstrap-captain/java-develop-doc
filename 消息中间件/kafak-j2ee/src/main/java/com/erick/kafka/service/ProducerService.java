package com.erick.kafka.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
@Slf4j
public class ProducerService<K, V> {
    @Resource(name = "producerWithDefaultPartition")
    private KafkaProducer producer;

    /**
     * 1. default partitioner: Stick Partitioner
     * 2. 根据key的hash，来对分区数取模后计算分区
     * 3. 根据指定的partition
     *
     * @param topicName
     * @param partition
     * @param key
     * @param value
     */
    /*异步发送*/
    public void asyncSend(String topicName, Integer partition, K key, V value) {
        producer.send(new ProducerRecord(topicName, partition, key, value));
    }


    /*带回调函数的异步发送*/
    public void asyncSendWithCallBack(String topicName, Integer partition, K key, V value) {
        producer.send(new ProducerRecord(topicName, partition, key, value), (metadata, e) -> {
            if (ObjectUtils.isEmpty(e)) {
                log.debug("Topic: {}, Partition: {}", metadata.topic(), metadata.partition());
            } else {
                log.error("Async Sending Request Error: {}", e.getMessage());
            }
        });
    }


    /*同步发送*/
    public void syncSend(String topicName, Integer partition, K key, V value) {
        try {
            Object result = producer.send(new ProducerRecord(topicName, partition, key, value)).get();

            if (ObjectUtils.isNotEmpty(result) || result instanceof RecordMetadata) {
                RecordMetadata metadata = (RecordMetadata) result;
                log.debug("Topic: {}, Partition: {}", metadata.topic(), metadata.partition());
            }
        } catch (Exception e) {
            log.error("Sync Sending Request Error:{}", e.getMessage());
        }
    }


    public void batchProduce(String topicName, int batchSize) {
        for (int i = 0; i < batchSize; i++) {
            producer.send(new ProducerRecord(topicName, "mock-data" + RandomStringUtils.randomAlphabetic(500)));
        }
    }
}
