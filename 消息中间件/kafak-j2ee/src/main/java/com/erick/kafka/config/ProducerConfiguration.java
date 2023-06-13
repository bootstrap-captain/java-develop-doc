package com.erick.kafka.config;

import com.erick.kafka.config.partition.ErickPartition;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Properties;


@Configuration
public class ProducerConfiguration {

    @Value("${kafka.server}")
    private String kafkaServer;

    /**
     * 相同类型的Bean注入后，优先使用 @Primary 的
     *
     * @return
     */
    @Bean(value = "producerWithDefaultPartition")
    public KafkaProducer<String, Object> getKafkaProducer() {
        Properties properties = getProperties();
        return new KafkaProducer(properties);
    }

    @Bean(value = "kafkaProducer")
    public KafkaProducer<String, Object> getKafkaProducerWithErickPartitioner() {
        Properties properties = getProperties();
        properties.put(ProducerConfig.PARTITIONER_CLASS_CONFIG, ErickPartition.class.getName());// 使用自定义分区器
        return new KafkaProducer(properties);
    }


    private Properties getProperties() {
        Properties properties = new Properties();
        properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaServer); // 节点
        properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class); //kafka自带序列化： key的序列化
        properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class); //kafka自带序列化： value的序列化
        properties.put(ProducerConfig.BUFFER_MEMORY_CONFIG, 33554432);// 缓冲区大小为32M
        properties.put(ProducerConfig.BATCH_SIZE_CONFIG, 16384);// batch.size: 16k
        properties.put(ProducerConfig.LINGER_MS_CONFIG, 5); // linger.ms: 5ms
        properties.put(ProducerConfig.ACKS_CONFIG, "all");   // ack设置
        properties.put(ProducerConfig.MAX_BLOCK_MS_CONFIG, 30000);
        properties.put(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, 1);// broker端最多能缓存的请求个数
        properties.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);  // 开启幂等
        // M1芯片支持问题： [FAILED_TO_LOAD_NATIVE_LIBRARY] no native library is found for os.name=Mac
        //properties.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, "snappy");// 压缩方式：
        properties.put(ProducerConfig.RETRIES_CONFIG, 10); // 重试次数， 默认为int最大值
        return properties;
    }
}
