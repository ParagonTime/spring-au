package org.pt.project.mapper;

import org.pt.project.dto.SearchUserDTO;
import org.pt.project.entity.SearchUser;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {
    public SearchUserDTO toDto(SearchUser searchUser) {
        return new SearchUserDTO(
                searchUser.getUserId(),
                searchUser.getRole(),
                searchUser.getEmail(),
                searchUser.getCreatedAt()
        );
    }
}

