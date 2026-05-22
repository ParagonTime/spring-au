package org.pt.project.controller;

import lombok.RequiredArgsConstructor;
import org.pt.project.dto.SearchTaskDto;
import org.pt.project.dto.SearchUserDTO;
import org.pt.project.service.SearchService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/search")
@RequiredArgsConstructor
public class SearchController {
    private final SearchService searchService;

    @GetMapping("/users/id")
    public SearchUserDTO searchUser(@RequestParam("id") String searchUserId,
                                    @RequestHeader("user-id") String userId
    ) {
        return searchService.searchUserById(searchUserId, userId);
    }

    @GetMapping("/users/email")
    public List<SearchUserDTO> searchUsersByEmail(@RequestParam("email") String email,
                                                      @RequestHeader("user-id") String userId
    ) {
        return searchService.searchUsersByEmail(email, userId);
    }

    @GetMapping("/tasks/{id}")
    public SearchTaskDto searchTaskById(@PathVariable("id") Long searchTaskId,
                                        @RequestHeader("user-id") String userId

    ) {
        return searchService.searchTaskById(searchTaskId, userId);
    }

}
