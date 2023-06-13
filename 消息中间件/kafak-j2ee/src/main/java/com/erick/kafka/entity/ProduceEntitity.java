package com.erick.kafka.entity;

import lombok.Data;

@Data
public class ProduceEntitity {
    private String topicName;
    private String key;
    private String value;
    private Integer partition;

    private String name;


    private String age;
}
