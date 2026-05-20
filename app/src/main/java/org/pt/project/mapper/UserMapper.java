package org.pt.project.mapper;

import org.pt.project.dto.UserDto;
import org.pt.project.entity.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public UserDto toUserDto(User user) {
        UserDto userDto = new UserDto();
        userDto.setId(user.getId());
        userDto.setUserId(user.getUserId());
        userDto.setEmail(userDto.getEmail());
        userDto.setLogin(userDto.getLogin());
        userDto.setRole(userDto.getRole());
        userDto.setCreatedAt(user.getCreatedAt());
        return userDto;
    }
}
