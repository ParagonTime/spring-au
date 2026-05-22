package org.pt.project.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.pt.project.dto.NewTaskRequest;
import org.pt.project.dto.TaskDto;
import org.pt.project.dto.UpdateTaskRequest;
import org.pt.project.entity.Task;
import org.pt.project.entity.TaskStatus;
import org.pt.project.entity.User;
import org.pt.project.event.TaskCreatedFlowEvent;
import org.pt.project.event.TaskDeletedFlowEvent;
import org.pt.project.event.TaskExecutorAssignedFlowEvent;
import org.pt.project.event.TaskStatusChangedFlowEvent;
import org.pt.project.event.TaskStreamEvent;
import org.pt.project.event.TaskUpdatedFlowEvent;
import org.pt.project.exception.NoFoundException;
import org.pt.project.mapper.TaskMapper;
import org.pt.project.repository.TaskRepository;
import org.pt.project.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.Instant;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final TaskMapper taskMapper;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${app.kafka.topics.task-stream}")
    private String taskStreamTopic;

    @Value("${app.kafka.topics.task-flow}")
    private String taskFlowTopic;

    @Transactional
    public TaskDto createTask(NewTaskRequest newTaskRequest, String userId) {
        log.info("Creating task with title: {}", newTaskRequest.getTitle());
        Task task = taskMapper.toTaskEntity(newTaskRequest);
        task.setUserId(UUID.fromString(userId));
        task.setStatus(TaskStatus.CREATED);
        task.setCreatedAt(Instant.now());
        Task savedTask = taskRepository.save(task);
        log.info("Task created with id: {}", savedTask.getId());

        TaskStreamEvent taskStreamEvent = new TaskStreamEvent(
                savedTask.getId(),
                savedTask.getTitle(),
                savedTask.getUserId(),
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
                kafkaTemplate.send(taskFlowTopic, taskCreatedFlowEvent);
            }
        });

        return taskMapper.toTaskDto(savedTask);
    }

    @Transactional(readOnly = true)
    public Page<TaskDto> getTasks(Pageable pageable, String userId) {
        log.debug("Getting tasks with pageable: {}", pageable);
        return taskRepository.findAllWithUser(pageable, userId).map(taskMapper::toTaskDto);
    }

    @Transactional(readOnly = true)
    public TaskDto getTaskById(Long taskId, String userId) {
        log.debug("Getting task by id: {}", taskId);
        Task task = taskRepository.findTaskById(taskId)
                .orElseThrow(() -> new NoFoundException("Task no found: " + taskId));

        return taskMapper.toTaskDto(task);
    }

    @Transactional
    public void setTaskExecutor(Long taskId, String ownerId, String userId) {
        log.info("Setting executor for task {} to user {}", taskId, userId);
        Task task = taskRepository.findTaskById(taskId)
                .orElseThrow(() -> new NoFoundException("Task no found: " + taskId));
        User owner = userRepository.findByUserId(ownerId)
                .orElseThrow(() -> new NoFoundException("User no found id: " + userId));
        if (!owner.getUserId().equals(task.getUserId())) {
            throw new AccessDeniedException("access denied");
        }
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new NoFoundException("User no found id: " + userId));
        task.setUserId(UUID.fromString(userId));

        taskRepository.save(task); // сделать разделение на ownerId из заголовка и userId из param на кого кидать task

        TaskStreamEvent taskStreamEvent = new TaskStreamEvent(
                task.getId(),
                task.getTitle(),
                task.getUserId(),
                task.getDescription(),
                task.getStatus(),
                task.getCreatedAt().toString()
        );

        Instant timeStamp = Instant.now();
        TaskExecutorAssignedFlowEvent taskExecutorAssignedFlowEvent = new TaskExecutorAssignedFlowEvent(
                task.getId(),
                timeStamp.toString()
        );

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                kafkaTemplate.send(taskStreamTopic, taskStreamEvent);
                kafkaTemplate.send(taskFlowTopic, taskExecutorAssignedFlowEvent);
            }
        });
    }

    @Transactional
    public void setTaskStatus(Long taskId, TaskStatus status, String userId) {
        log.info("Setting task {} status to {}", taskId, status);
        Task task = taskRepository.findTaskById(taskId)
                .orElseThrow(() -> new NoFoundException("Task no found: " + taskId));
        User user = userRepository.findByUserId(userId)
                        .orElseThrow(() -> new NoFoundException("User no found id: " + userId));
        if (!user.getUserId().equals(task.getUserId())) {
            throw new AccessDeniedException("access denied");
        }

        task.setStatus(status);
        taskRepository.save(task);

        TaskStreamEvent taskStreamEvent = new TaskStreamEvent(
                task.getId(),
                task.getTitle(),
                task.getUserId(),
                task.getDescription(),
                task.getStatus(),
                task.getCreatedAt().toString()
        );

        Instant timeStamp = Instant.now();
        TaskStatusChangedFlowEvent taskStatusChangedFlowEvent = new TaskStatusChangedFlowEvent(
                task.getId(),
                task.getStatus(),
                timeStamp.toString()
        );

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                kafkaTemplate.send(taskStreamTopic, taskStreamEvent);
                kafkaTemplate.send(taskFlowTopic, taskStatusChangedFlowEvent);
            }
        });
    }

    @Transactional
    public TaskDto updateTask(Long taskId, String userId, UpdateTaskRequest request) {
        log.info("Updating task {} by user {}", taskId, userId);
        Task task = taskRepository.findTaskById(taskId)
                .orElseThrow(() -> new NoFoundException("Task no found: " + taskId));
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new NoFoundException("User no found id: " + userId));
        if (!user.getUserId().equals(task.getUserId())) {
            throw new AccessDeniedException("Access denied");
        }

        if (request.getTitle() != null && !request.getTitle().isBlank()) {
            task.setTitle(request.getTitle());
        }
        if (request.getUserId() != null && !request.getUserId().isBlank()) {
            task.setUserId(UUID.fromString(request.getUserId()));
        }
        if (request.getDescription() != null && !request.getDescription().isBlank()) {
            task.setDescription(request.getDescription());
        }

        taskRepository.delete(task);

        TaskStreamEvent taskStreamEvent = new TaskStreamEvent(
                task.getId(),
                task.getTitle(),
                task.getUserId(),
                task.getDescription(),
                task.getStatus(),
                task.getCreatedAt().toString()
        );
        Instant timeStamp = Instant.now();
        TaskUpdatedFlowEvent taskUpdatedFlowEvent = new TaskUpdatedFlowEvent(
                task.getId(),
                timeStamp.toString()
        );
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                kafkaTemplate.send(taskStreamTopic, taskStreamEvent);
                kafkaTemplate.send(taskFlowTopic, taskUpdatedFlowEvent);
            }
        });

        return taskMapper.toTaskDto(task);
    }

    @Transactional
    public void deleteTask(Long taskId, String userId) {
        log.info("Deleting task {} by user {}", taskId, userId);
        Task task = taskRepository.findTaskById(taskId)
                .orElseThrow(() -> new NoFoundException("Task no found: " + taskId));
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new NoFoundException("User no found id: " + userId));
        if (!user.getUserId().equals(task.getUserId())) {
            throw new AccessDeniedException("Access denied");
        }

        taskRepository.delete(task);

        TaskStreamEvent taskStreamEvent = new TaskStreamEvent(
                task.getId(),
                task.getTitle(),
                task.getUserId(),
                task.getDescription(),
                task.getStatus(),
                task.getCreatedAt().toString()
        );

        Instant timeStamp = Instant.now();
        TaskDeletedFlowEvent taskDeletedFlowEvent = new TaskDeletedFlowEvent(
                task.getId(),
                timeStamp.toString()
        );

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                kafkaTemplate.send(taskStreamTopic, taskStreamEvent);
                kafkaTemplate.send(taskFlowTopic, taskDeletedFlowEvent);
            }
        });
    }
}
