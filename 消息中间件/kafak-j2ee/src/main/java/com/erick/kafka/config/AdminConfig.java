package com.erick.kafka.config;

import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.admin.AdminClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Properties;

@Configuration
public class AdminConfig {

    @Value("${kafka.server}")
    private String kafkaServer;

    /**
     * 配置时候： 连接所有的kafka的节点，避免单节点故障问题
     *
     * @return
     */
    @Bean
    public AdminClient getAdminClient() {
        Properties properties = new Properties();
        properties.put(CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG, kafkaServer);
        AdminClient adminClient = AdminClient.create(properties);
        return adminClient;
    }
}
