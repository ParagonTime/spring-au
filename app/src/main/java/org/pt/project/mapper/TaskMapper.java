package org.pt.project.mapper;

import lombok.RequiredArgsConstructor;
import org.pt.project.dto.NewTaskRequest;
import org.pt.project.dto.TaskDto;
import org.pt.project.entity.Task;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TaskMapper {

    private final UserMapper userMapper;

    public TaskDto toTaskDto(Task task) {
        TaskDto taskDto = new TaskDto();
        taskDto.setId(task.getId());
        taskDto.setDescription(task.getDescription());
        taskDto.setStatus(task.getStatus());
        taskDto.setTitle(task.getTitle());
        taskDto.setCreatedAt(task.getCreatedAt());
        if (task.getUser() != null) {
            taskDto.setUser(userMapper.toUserDto(task.getUser()));
        }
        return taskDto;
    }

    public Task toTaskEntity(NewTaskRequest newTaskRequest) {
        Task task = new Task();
        task.setTitle(newTaskRequest.getTitle());
        if (newTaskRequest.getDescription() != null) {
            task.setDescription(newTaskRequest.getDescription());
        }
        return task;
    }
}
