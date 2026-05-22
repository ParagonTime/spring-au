package org.pt.project.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;

import org.pt.project.dto.NewTaskRequest;
import org.pt.project.dto.TaskDto;
import org.pt.project.dto.UpdateTaskRequest;
import org.pt.project.entity.TaskStatus;
import org.pt.project.service.TaskService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TaskDto createTask(@Valid @RequestBody NewTaskRequest newTaskRequest,
                              @RequestHeader("user-id") String userId
    ) {
        return taskService.createTask(newTaskRequest, userId);
    }

    @GetMapping
    public Page<TaskDto> getTasks(Pageable pageable,
                                  @RequestHeader("user-id") String userId
    ) {
        return taskService.getTasks(pageable, userId);
    }

    @GetMapping("/{id}")
    public TaskDto getTaskById(@Positive @PathVariable("id") Long taskId,
                               @RequestHeader("user-id") String userId
    ) {
        return taskService.getTaskById(taskId, userId);
    }

    @PatchMapping("/{id}/executor")
    public void setTaskExecutor(@Positive @PathVariable("id") Long taskId,
                                @RequestParam("userId") String userId,
                                @RequestHeader("user-id") String ownerId
    ) {
        taskService.setTaskExecutor(taskId, ownerId, userId);
    }

    @PatchMapping("/{id}/status/{status}")
    public void setTaskStatus(@Positive @PathVariable("id") Long taskId,
                              @NotNull @PathVariable("status") TaskStatus status,
                              @RequestHeader("user-id") String userId

    ) {
        taskService.setTaskStatus(taskId, status, userId);
    }

    @PutMapping("/{id}")
    public TaskDto updateTask(@PathVariable("id") Long taskId,
                              @RequestHeader("user-id") String userId,
                              @RequestBody UpdateTaskRequest request
    ) {
        return taskService.updateTask(taskId, userId, request);
    }

    @DeleteMapping("/{id}")
    public void deleteTask(@PathVariable("id") Long taskId,
                           @RequestHeader("user-id") String userId
    ) {
        taskService.deleteTask(taskId, userId);
    }
}
