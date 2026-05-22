package org.pt.project.mapper;

import org.pt.project.dto.SearchTaskDto;
import org.pt.project.entity.SearchTask;
import org.springframework.stereotype.Component;

@Component
public class TaskMapper {
    public SearchTaskDto toDto(SearchTask searchTask) {
        return new SearchTaskDto(
                searchTask.getTaskId(),
                searchTask.getTitle(),
                searchTask.getDescription(),
                searchTask.getStatus(),
                searchTask.getUserId(),
                searchTask.getCreatedAt()
        );
    }
}
