package com.erick.boot.kafka.service;

import org.springframework.stereotype.Service;

@Service
public class MessageProcessor {

    public void processMessage(String message){
        System.out.println(message);
    }
}
