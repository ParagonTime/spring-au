package org.pt.project.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.pt.project.dto.NewTaskRequest;
import org.pt.project.dto.TaskDto;
import org.pt.project.entity.Task;
import org.pt.project.entity.TaskStatus;
import org.pt.project.entity.User;
import org.pt.project.event.ExecutorAssignedEvent;
import org.pt.project.event.StatusChangedEvent;
import org.pt.project.event.TaskCreatedFlowEvent;
import org.pt.project.event.TaskStreamEvent;
import org.pt.project.exception.NoFoundException;
import org.pt.project.mapper.TaskMapper;
import org.pt.project.mapper.UserMapper;
import org.pt.project.repository.TaskRepository;
import org.pt.project.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.Instant;

@Slf4j
@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final TaskMapper taskMapper;
    private final UserMapper userMapper;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${app.kafka.topics.task-stream}")
    private String taskStreamTopic;

    @Value("${app.kafka.topics.task-flow}")
    private String taskCreatedFlowTopic;

    @Value("${app.kafka.topics.executor-assigned}")
    private String executorAssignedTopic;

    @Value("${app.kafka.topics.status-changed}")
    private String statusChangedTopic;

    @Transactional
    public TaskDto createTask(NewTaskRequest newTaskRequest, Long userId) {
        log.info("Creating task with title: {}", newTaskRequest.getTitle());
        Task task = taskMapper.toTaskEntity(newTaskRequest);
        if (newTaskRequest.getUserId() != null) {
            User user = userRepository.findById(newTaskRequest.getUserId())
                    .orElseThrow(() -> new NoFoundException("User no found id: " + newTaskRequest.getUserId()));
            task.setUser(user);
        }
        task.setStatus(TaskStatus.CREATED);
        task.setCreatedAt(Instant.now());
        Task savedTask = taskRepository.save(task);
        log.info("Task created with id: {}", savedTask.getId());

        TaskStreamEvent taskStreamEvent = new TaskStreamEvent(
                savedTask.getId(),
                savedTask.getTitle(),
                savedTask.getUser() != null ? userMapper.toUserDto(savedTask.getUser()) : null,
                savedTask.getDescription(),
                savedTask.getStatus(),
                savedTask.getCreatedAt().toString()
        );

        TaskCreatedFlowEvent taskCreatedFlowEvent = new TaskCreatedFlowEvent(
                savedTask.getId(),
                savedTask.getCreatedAt().toString()
        );

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                kafkaTemplate.send(taskStreamTopic, taskStreamEvent);
                kafkaTemplate.send(taskCreatedFlowTopic, taskCreatedFlowEvent);
            }
        });

        return taskMapper.toTaskDto(savedTask);
    }

    @Transactional(readOnly = true)
    public Page<TaskDto> getTasks(Pageable pageable, Long userId) {
        log.debug("Getting tasks with pageable: {}", pageable);
        return taskRepository.findAllWithUser(pageable).map(taskMapper::toTaskDto);
    }

    @Transactional(readOnly = true)
    public TaskDto getTaskById(Long taskId, Long userId) {
        log.debug("Getting task by id: {}", taskId);
        Task task = taskRepository.findTaskById(taskId)
                .orElseThrow(() -> new NoFoundException("Task no found: " + taskId));

        return taskMapper.toTaskDto(task);
    }

    @Transactional
    public void setTaskExecutor(Long taskId, Long userId) {
        log.info("Setting executor for task {} to user {}", taskId, userId);
        Task task = taskRepository.findTaskById(taskId)
                .orElseThrow(() -> new NoFoundException("Task no found: " + taskId));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NoFoundException("User no found id: " + userId));
        task.setUser(user);

        taskRepository.save(task);


        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                kafkaTemplate.send(executorAssignedTopic, new ExecutorAssignedEvent(taskId, userId));
            }
        });
    }

    @Transactional
    public void setTaskStatus(Long taskId, TaskStatus status, Long userId) {
        log.info("Setting task {} status to {}", taskId, status);

        Task task = taskRepository.findTaskById(taskId)
                .orElseThrow(() -> new NoFoundException("Task no found: " + taskId));
        task.setStatus(status);

        taskRepository.save(task);

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                kafkaTemplate.send(statusChangedTopic, new StatusChangedEvent(taskId, status));
            }
        });
    }
}
