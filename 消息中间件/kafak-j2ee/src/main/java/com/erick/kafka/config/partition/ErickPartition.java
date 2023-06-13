package com.erick.kafka.config.partition;

import org.apache.kafka.clients.producer.Partitioner;
import org.apache.kafka.common.Cluster;

import java.util.Map;

/**
 * 自定义分区器: 根据key来进行
 */
public class ErickPartition implements Partitioner {
    @Override
    public int partition(String topic, Object key, byte[] keyBytes, Object value, byte[] valueBytes, Cluster cluster) {
        int partition;

        if (key.toString().contains("erick")) {
            partition = 0;
        } else if (key.toString().contains("tom")) {
            partition = 1;
        } else {
            partition = 2;
        }
        return partition;
    }

    @Override
    public void close() {
    }

    @Override
    public void configure(Map<String, ?> configs) {
    }
}
