package org.pt.project.controller;

import lombok.RequiredArgsConstructor;
import org.pt.project.event.TaskStreamEvent;
import org.pt.project.event.UserStreamEvent;
import org.pt.project.service.SearchService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SearchKafkaListeners {
    private final SearchService searchService;

    @KafkaListener(topics = "${search.kafka.topics.user-stream}")
    public void handleUserStream(UserStreamEvent event) {
        searchService.handleUserStreamEvent(event);
    }

    @KafkaListener(topics = "${search.kafka.topics.task-stream}")
    public void handleTaskStream(TaskStreamEvent event) {
        searchService.handleTaskStreamEvent(event);
    }
}
