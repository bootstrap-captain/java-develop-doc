package com.erick.kafka.controller;

import com.erick.kafka.service.TopicService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;

@RequestMapping("/topic")
@RestController
@RequiredArgsConstructor
public class TopicController {

    private final TopicService topicService;

    @GetMapping("/listTopics")
    public Set<String> listTopics() {
        return topicService.listTopics();
    }

    @GetMapping("/createTopics")
    public void createTopics(String topicName) {
        topicService.createTopic(topicName);
    }

    @GetMapping("/deleteTopics")
    public void deleteTopics(String topicNames) {
        topicService.deleteTopic(topicNames);
    }
}
