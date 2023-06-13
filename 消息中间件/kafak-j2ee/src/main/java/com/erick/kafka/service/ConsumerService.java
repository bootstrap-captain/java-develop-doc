package com.erick.kafka.service;

import com.erick.kafka.config.ConsumerConfiguration;
import com.google.common.collect.Lists;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ConsumerService {

    //private static final String TOPIC_NAME = "_consumer_offsets";
    private final KafkaConsumer<String, String> consumer;

    private final ConsumerConfiguration consumerConfiguration;

    public void consumeTopic(String topicName) {
        // 订阅一定的topicName
        consumer.subscribe(Lists.newArrayList(topicName));
        // 消费数据
        pollMessage(consumer);
    }

    public void consumeTopicPartition(String topicName, Integer partitionNo) {
        // 消费某个topic的某个分区
        consumer.assign(Lists.newArrayList(new TopicPartition(topicName, partitionNo)));
        pollMessage(consumer);
    }

    public void consumeWithMultiConsumer(String topicName) {

        List<KafkaConsumer<String, String>> consumers = Lists.newArrayList(
                consumerConfiguration.getKafkaConsumer(), consumerConfiguration.getKafkaConsumer(),
                consumerConfiguration.getKafkaConsumer());

        // 消费者组订阅对应的topic
        for (KafkaConsumer<String, String> consumer : consumers) {
            consumer.subscribe(Lists.newArrayList(topicName));
        }
        // 同时多线程去拉取任务
        multiConsumerExecute(consumers);
    }

    private void multiConsumerExecute(List<KafkaConsumer<String, String>> consumers) {
        for (KafkaConsumer<String, String> consumer : consumers) {
            new Thread(() -> {
                pollMessage(consumer);
            }).start();
        }
    }

    /**
     * 不断拉取消息
     *
     * @param consumer
     */
    private void pollMessage(KafkaConsumer<String, String> consumer) {
        while (true) {
            // 间隔几秒拉取一次
            ConsumerRecords<String, String> results = consumer.poll(Duration.ofSeconds(3));
            log.debug("{} ===== messages begin poll", consumer);
            for (ConsumerRecord<String, String> record : results) {
                log.debug("{} ===== data fetched: {}", consumer, record, record.value());
            }
        }
    }
}
