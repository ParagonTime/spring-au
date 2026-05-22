package org.pt.project.controller;

import lombok.RequiredArgsConstructor;
import org.pt.project.dto.UpdateUserRequest;
import org.pt.project.entity.User;
import org.pt.project.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping("")
    public User getUser(@RequestHeader("user-id") String userId) {
        return userService.getUserByUserId(userId);
    }

    @PutMapping("")
    public User updateUser(@RequestHeader("user-id") String userId,
                           @RequestBody UpdateUserRequest req
    ) {
        return userService.updateUser(userId, req);
    }

    @DeleteMapping("")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUser(@RequestHeader("user-id") String userId) {
        userService.deleteUser(userId);
    }
}
