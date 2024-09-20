package com.example.game.dto.mapper;

import com.example.game.db.model.User;
import com.example.game.dto.model.UserDto;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper extends BaseEntityMapper<User, UserDto> {

	@Override
	UserDto convert(User user);
}
