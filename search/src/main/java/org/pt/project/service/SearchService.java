package org.pt.project.service;

import lombok.RequiredArgsConstructor;
import org.pt.project.dto.SearchTaskDto;
import org.pt.project.dto.SearchUserDTO;
import org.pt.project.entity.SearchTask;
import org.pt.project.entity.SearchUser;
import org.pt.project.event.TaskStreamEvent;
import org.pt.project.event.UserStreamEvent;
import org.pt.project.exception.NoFoundException;
import org.pt.project.mapper.TaskMapper;
import org.pt.project.mapper.UserMapper;
import org.pt.project.repository.SearchTaskRepository;
import org.pt.project.repository.SearchUserRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SearchService {

    private final TaskMapper taskMapper;
    private final UserMapper userMapper;
    private final SearchTaskRepository searchTaskRepository;
    private final SearchUserRepository searchUserRepository;

    // тест
    public SearchUserDTO searchUserById(String searchUserId, String userId) {
        SearchUser searchUser = searchUserRepository.findByUserId(UUID.fromString(searchUserId))
                .orElseThrow(() -> new NoFoundException("Not found user by id " + searchUserId));

        return userMapper.toDto(searchUser);
    }

    public SearchTaskDto searchTaskById(Long searchTaskId, String userId) {
        SearchTask searchTask = searchTaskRepository.findByTaskId(searchTaskId)
                .orElseThrow(() -> new NoFoundException("No found task by id " + searchTaskId));

        return taskMapper.toDto(searchTask);
    }

    public void handleUserStreamEvent(UserStreamEvent event) {
        SearchUser user = searchUserRepository.findByUserId(event.userId())
                .orElse(new SearchUser());
        user.setUserId(event.userId());
        user.setEmail(event.email());
        user.setRole(event.role());
        user.setCreatedAt(Instant.parse(event.createdAt()));

        searchUserRepository.save(user);
    }

    public void handleTaskStreamEvent(TaskStreamEvent event) {
        SearchTask task = searchTaskRepository.findByTaskId(event.taskId())
                .orElse(new SearchTask());
        task.setTaskId(event.taskId());
        task.setUserId(event.userId());
        task.setStatus(event.status());
        task.setTitle(event.title());
        task.setDescription(event.description());
        task.setCreatedAt(Instant.parse(event.createdAt()));

        searchTaskRepository.save(task);
    }

    public List<SearchUserDTO> searchUsersByEmail(String email, String userId) {
        List<SearchUser> searchUserList = searchUserRepository.findByEmail(email);
        return searchUserList.stream()
                .map(userMapper::toDto)
                .toList();
    }
}
